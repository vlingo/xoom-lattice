// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import io.vlingo.lattice.exchange.ExchangeException;
import io.vlingo.lattice.exchange.MessageParameters;

/**
 * A message producer that facilitates sending messages to a
 * BrokerChannel, either a BrokerExchange or a BrokerQueue.
 */
class MessageProducer {

  /** My broker connection, which is where I send messages. */
  private final BrokerConnection brokerConnection;

  /**
   * Answer a new instance of a MessageProducer.
   * @param brokerChannel the BrokerChannel where messages are to be sent
   * @return MessageProducer
   */
  public static MessageProducer instance(final BrokerConnection brokerChannel) {
    return new MessageProducer(brokerChannel);
  }

  /**
   * Close me, which closes my broker channel.
   */
  public void close() {
    brokerConnection.close();
  }

  /**
   * Answers the receiver after sending binaryMessage to my channel. This is a
   * producer ignorance way to use either an exchange or a queue channel without
   * requiring it to pass specific parameters. By answering myself I allow for
   * sending message bursts.
   * @param binaryMessage the byte[] binary message to send
   * @param messageParameters the MessageParameters
   * @return MessageProducer
   */
  public MessageProducer send(final byte[] binaryMessage, final MessageParameters messageParameters) {
    check(messageParameters);

    try {
      brokerConnection.channel().basicPublish(brokerConnection.exchangeName(), brokerConnection.queueName(),
              binaryDurability(), binaryMessage);
    } catch (IOException e) {
      throw new ExchangeException("Failed to send message to channel because: " + e.getMessage(), e);
    }
    return this;
  }

  /**
   * Answers the receiver after sending binaryMessage to my channel with
   * routingKey. This is a producer ignorance way to use an exchange without
   * requiring it to pass the exchange name. By answering myself I allow for
   * sending message bursts.
   * @param routingKey the String routing key
   * @param binaryMessage the byte[] binary message to send
   * @param messageParameters the MessageParameters
   * @return MessageProducer
   */
  public MessageProducer send(final String routingKey, final byte[] binaryMessage, final MessageParameters messageParameters) {
    check(messageParameters);

    try {
      brokerConnection.channel().basicPublish(brokerConnection.exchangeName(), routingKey,
              binaryDurability(), binaryMessage);
    } catch (IOException e) {
      throw new ExchangeException("Failed to send message to channel because: " + e.getMessage(), e);
    }
    return this;
  }

  /**
   * Answers the receiver after sending binaryMessage to my channel with exchange
   * and routingKey. By answering myself I allow for sending message bursts.
   * @param exchange the String name of the exchange
   * @param routingKey the String routing key
   * @param binaryMessage the byte[] binary message to send
   * @param messageParameters the MessageParameters
   * @return MessageProducer
   */
  public MessageProducer send(final String exchange, final String routingKey, final byte[] binaryMessage, final MessageParameters messageParameters) {
    check(messageParameters);

    try {
      brokerConnection.channel().basicPublish(exchange, routingKey, binaryDurability(), binaryMessage);
    } catch (IOException e) {
      throw new ExchangeException("Failed to send message to channel because: " + e.getMessage(), e);
    }
    return this;
  }

  /**
   * Constructs my default state.
   * @param brokerConnection the BrokerChannel to which I send messages
   */
  protected MessageProducer(final BrokerConnection brokerConnection) {
    this.brokerConnection = brokerConnection;
  }

  /**
   * Checks messageParameters for validity.
   * @param messageParameters the MessageParameters to check
   */
  private void check(final MessageParameters messageParameters) {
    if (brokerConnection.durable) {
      if (!messageParameters.isDurableDeliveryMode()) {
        throw new IllegalArgumentException("MessageParameters must be durable.");
      }
    } else {
      if (messageParameters.isDurableDeliveryMode()) {
        throw new IllegalArgumentException("MessageParameters must not be durable.");
      }
    }
  }

  /**
   * Answers the binary durability BasicProperties according to the
   * brokerChannel's durability.
   * @return BasicProperties
   */
  private BasicProperties binaryDurability() {
    return brokerConnection.durable ? MessageProperties.PERSISTENT_BASIC : null;
  }
}
