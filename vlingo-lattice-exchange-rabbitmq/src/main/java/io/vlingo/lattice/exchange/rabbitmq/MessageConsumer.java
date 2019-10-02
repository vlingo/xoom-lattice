// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import io.vlingo.lattice.exchange.ExchangeException;
import io.vlingo.lattice.exchange.MessageParameters;

/**
 * A message consumer, which facilitates receiving messages from a BrokerQueue. A
 * MessageListener or a client may close me, terminating message consumption.
 */
class MessageConsumer {

  /** My autoAcknowledged property. */
  private final boolean autoAcknowledged;

  /** My queue, which is where my messages come from. */
  private final BrokerQueue queue;

  /** My closed property, which indicates I have been closed. */
  private boolean closed;

  /** My tag, which is produced by the broker. */
  private String tag;

  /**
   * Answer a new auto-acknowledged MessageConsumer, which means all messages received
   * are automatically considered acknowledged as received from the broker.
   * @param queue the BrokerQueue from which messages are received
   * @return MessageConsumer
   */
  static MessageConsumer autoAcknowledgedInstance(final BrokerQueue queue) {
    return MessageConsumer.instance(queue, true);
  }

  /**
   * Answer a new MessageConsumer with manual acknowledgment.
   * @param queue the BrokerQueue from which messages are received
   * @return MessageConsumer
   */
  static MessageConsumer instance(final BrokerQueue queue) {
    return new MessageConsumer(queue, false);
  }

  /**
   * Answer a new MessageConsumer with acknowledgment managed per autoAcknowledged.
   * @param queue the BrokerQueue from which messages are received
   * @param autoAcknowledged the boolean indicating whether or not auto-acknowledgment is used
   * @return MessageConsumer
   */
  static MessageConsumer instance(final BrokerQueue queue, final boolean autoAcknowledged) {
    return new MessageConsumer(queue, autoAcknowledged);
  }

  /**
   * Closes me, which closes my queue.
   */
  void close() {
    closed = true;
    queue.close();
  }

  /**
   * Answer whether or not I have been closed.
   * @return boolean
   */
  boolean isClosed() {
    return closed;
  }

  /**
   * Receives all messages on a separate thread and dispatches them to
   * messageListener until I am closed or until the broker is shut down.
   * @param messageListener the MessageListener that handles messages
   */
  void receiveAll(final MessageListener messageListener) {
    receiveFor(messageListener);
  }

  /**
   * Answers my tag, which was produced by the broker.
   * @return String
   */
  String tag() {
    return tag;
  }

  /**
   * Constructs my default state.
   * @param queue the Queue from which I receive messages
   * @param autoAcknowledged the boolean indicating whether or not auto-acknowledgment is used
   */
  protected MessageConsumer(final BrokerQueue queue, boolean autoAcknowledged) {
    this.queue = queue;
    this.autoAcknowledged = autoAcknowledged;
    this.tag = "";

    equalizeDistribution();
  }

  /**
   * Answer my queue.
   * @return BrokerQueue
   */
  protected BrokerQueue queue() {
    return queue;
  }

  /**
   * Cause the RabbitMQ queue to equalize distribution of
   * messages equally among all queue consumers.
   */
  private void equalizeDistribution() {
    try {
      queue.connection.channel().basicQos(1);
    } catch (IOException e) {
      throw new ExchangeException("Cannot equalize distribution.", e);
    }
  }

  /**
   * Register messageListener with the channel indirectly using a DispatchingConsumer.
   * @param messageListener the MessageListener
   */
  private void receiveFor(final MessageListener messageListener) {
    final Channel channel = queue.connection.channel();

    try {
      tag = channel.basicConsume(queue.name(), autoAcknowledged,
              new DispatchingConsumer(channel, messageListener));
    } catch (IOException e) {
      throw new ExchangeException("Failed to initiate consume because: " + e.getMessage(), e);
    }
  }

  /**
   * Consumer of RabbitMQ queue messages.
   */
  private class DispatchingConsumer extends DefaultConsumer {
    private final MessageListener messageListener;

    public DispatchingConsumer(final Channel channel, final MessageListener messageListener) {
      super(channel);
      this.messageListener = messageListener;
    }

    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, final BasicProperties properties, final byte[] body) throws IOException {
      if (!isClosed()) {
        final MessageParameters parameters =
                MessageParameters
                  .bare()
                  .deliveryId(Long.toString(envelope.getDeliveryTag()))
                  .exchangeName(envelope.getExchange())
                  .tag(consumerTag)
                  .redeliver(envelope.isRedeliver())
                  .routing(envelope.getRoutingKey());
        handle(messageListener, new Message(body, parameters));
      }
      if (isClosed()) {
        queue().close();
      }
    }

    @Override
    public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException aSignal) {
      close();
    }

    private void handle(final MessageListener messageListener, final Message message) {
      try {
        messageListener.handleMessage(message);
        ack(message);
      } catch (ExchangeException e) {
        nack(message, e.retry());
      } catch (Throwable t) {
        nack(message, false);
      }
    }

    private void ack(final Message message) {
      try {
        if (!autoAcknowledged) {
          getChannel().basicAck(Long.parseLong(message.messageParameters.deliveryId()), false);
        }
      } catch (IOException ioe) {
        // fall through
      }
    }

    private void nack(final Message message, final boolean retry) {
      try {
        if (!autoAcknowledged) {
          getChannel().basicNack(Long.parseLong(message.messageParameters.deliveryId()), false, retry);
        }
      } catch (IOException ioe) {
        // fall through
      }
    }
  }
}
