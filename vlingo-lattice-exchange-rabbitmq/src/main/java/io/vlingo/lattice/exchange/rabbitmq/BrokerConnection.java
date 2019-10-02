// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.vlingo.lattice.exchange.ConnectionSettings;

/**
 * Facilitates connections to the RabbitMQ message broker.
 */
class BrokerConnection {
  /** The type of connection. */
  enum Type { Exchange, Queue };

  /** My connectionSettings. */
  final ConnectionSettings connectionSettings;

  /** My durable property, which indicates whether or not messages are durable. */
  final boolean durable;

  /** My hostName, which is the hostName of the broker. There may be a :port appended. */
  final String hostName;

  /** My name. */
  final String name;

  /** My type. */
  final Type type;

  /** My channel. */
  private Channel channel;

  /** My close flag. */
  private boolean closed;

  /** My connection, which is the connection to my host broker. */
  private Connection connection;

  /**
   * Constructs my default state.
   * @param connectionSettings the ConnectionSettings
   * @param name the String name of my implementor
   * @param isDurable the boolean indicating channel durability
   */
  BrokerConnection(final ConnectionSettings connectionSettings, final Type type, final String name, final boolean isDurable) {
    this.connectionSettings = connectionSettings;
    final ConnectionFactory factory = configureConnectionFactoryUsing(connectionSettings);
    this.type = type;
    this.name = name != null ? name : "";
    this.hostName = connectionSettings.hostName;
    this.durable = isDurable;

    try {
      this.connection = factory.newConnection();
      this.channel = connection.createChannel();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create/open the queue because: " + e.getMessage(), e);
    }
  }

  /**
   * Constructs my default state.
   * @param brokerChannel the BrokerChannel to initialize with
   */
  BrokerConnection(final BrokerConnection brokerChannel, final Type type) {
    this(brokerChannel, type, null);
  }

  /**
   * Constructs my default state.
   * @param brokerChannel the BrokerChannel to initialize with
   * @param type the Type using me
   * @param name the String name of my implementor
   */
  BrokerConnection(final BrokerConnection brokerChannel, final Type type, final String name) {
    this(brokerChannel, type, name, brokerChannel.durable);
  }

  /**
   * Constructs my default state.
   * @param brokerChannel the BrokerChannel to initialize with
   * @param type the Type using me
   * @param name the String name of my implementor
   * @param isDurable the boolean indicating channel durability
   */
  BrokerConnection(final BrokerConnection brokerChannel, final Type type, final String name, final boolean isDurable) {
    this.connectionSettings = brokerChannel.connectionSettings;
    this.hostName = brokerChannel.hostName;
    this.type = type;
    this.name = name != null ? name : "";
    this.connection = brokerChannel.connection;
    this.channel = brokerChannel.channel;
    this.durable = isDurable;
  }

  /**
   * Answer my channel.
   * @return Channel
   */
  Channel channel() {
    return channel;
  }

  /**
   * Closes me.
   */
  void close() {
    // RabbitMQ doesn't guarantee that if isOpen()
    // answers true that close() will work because
    // another client may be racing to close the
    // same process and/or components. so here just
    // attempt to close, catch and ignore, and move
    // on to next steps is the recommended approach.
    //
    // for the purpose here, the isOpen() checks prevent
    // closing a shared channel and connection that is
    // shared by a subscriber exchange and queue.

    try {
      if (!closed && channel.isOpen()) {
        channel.close();
        connection.close();
      }
    } catch (Throwable e) {
      // fall through
    }

    try {
      if (connection != null && connection.isOpen()) {
        connection.close();
      }
    } catch (Throwable e) {
      // fall through
    }

    closed = true;
    channel = null;
    connection = null;
  }

  /**
   * Answer whether or not I am closed.
   * @return boolean
   */
  boolean isClosed() {
    return closed;
  }

  /**
   * Answers whether or not I am an exchange channel.
   * @return boolean
   */
  boolean isExchange() {
    return type == Type.Exchange;
  }

  /**
   * Answer my name as the exchange name if I am an Exchange; otherwise the empty String.
   * @return String
   */
  String exchangeName() {
    return isExchange() ? name : "";
  }

  /**
   * Answers whether or not I am a queue channel.
   * @return boolean
   */
  boolean isQueue() {
    return type == Type.Queue;
  }

  /**
   * Answers my name as the queue name if I am a Queue; otherwise the empty String.
   * @return String
   */
  String queueName() {
    return this.isQueue() ? name : "";
  }

  /**
   * Answer a new ConnectionFactory configured with aConnectionSettings.
   * @param aConnectionSettings the ConnectionFactory
   * @return ConnectionFactory
   */
  private ConnectionFactory configureConnectionFactoryUsing(final ConnectionSettings connectionSettings) {
    final ConnectionFactory factory = new ConnectionFactory();

    factory.setHost(connectionSettings.hostName);

    if (connectionSettings.hasPort()) {
      factory.setPort(connectionSettings.port);
    }

    factory.setVirtualHost(connectionSettings.virtualHost);

    if (connectionSettings.hasUserCredentials()) {
      factory.setUsername(connectionSettings.username);
      factory.setPassword(connectionSettings.password);
    }

    return factory;
  }
}
