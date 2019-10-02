// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

import io.vlingo.lattice.exchange.ConnectionSettings;
import io.vlingo.lattice.exchange.Covey;
import io.vlingo.lattice.exchange.Exchange;
import io.vlingo.lattice.exchange.Forwarder;
import io.vlingo.lattice.exchange.Queue;
import io.vlingo.lattice.exchange.rabbitmq.BrokerConnection.Type;

/**
 * A Queue for RabbitMQ via a BrokerChannel.
 */
class BrokerQueue implements Queue {

  /** My connection to the broker. */
  final BrokerConnection connection;

  /** My exchange name. */
  private final String exchangeName;

  /** My forwarder. */
  private final Forwarder forwarder;

  /** My queue name. */
  private final String queueName;

  //====================================
  // Queue
  //====================================

  @Override
  public void close() {
    connection.close();
  }

  /*
   * @see io.vlingo.lattice.exchange.Queue#channel()
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T channel() {
    return (T) connection.channel();
  }

  /*
   * @see io.vlingo.lattice.exchange.Queue#connection()
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T connection() {
    return (T) connection;
  }

  @Override
  public String name() {
    return queueName;
  }

  @Override
  public <L, E, EX> Exchange register(final Covey<L, E, EX> covey) {
    forwarder.register(covey);
    return this;
  }

  @Override
  public <L> void send(final L local) {
    forwarder.forwardToSender(local);
  }

  /**
   * Attach to the queue named queueName using the connection channel of exchange.
   * @param exchange the BrokerExchange
   * @param queueName the String queue name
   */
  static BrokerQueue using(final BrokerExchange exchange, final String queueName) {
    return new BrokerQueue(exchange, queueName);
  }
  /**
   * Constructs my default state.
   * @param brokerExchange the BrokerExchange to initialize with
   * @param queueName the String name of the exchange, or the empty string
   * @param aType the String type of the exchange
   * @param isDurable the boolean indicating whether or not I am durable
   * @param isExclusive the boolean indicating whether or not I am exclusive
   * @param isAutoDeleted the boolean indicating whether or not I should be auto-deleted
   */
  static BrokerQueue using(
          final BrokerExchange exchange,
          final String queueName,
          final boolean isDurable,
          final boolean isExclusive,
          final boolean isAutoDeleted) {
    return new BrokerQueue(exchange, queueName, isDurable, isExclusive, isAutoDeleted);
  }

  /**
   * Constructs my default state.
   * @param connectionSettings the ConnectionSettings
   * @param exchangeName the String name of the exchange, or the empty string
   * @param queueName the String name of the queue
   * @param isDurable the boolean indicating whether or not I am durable
   * @param isExclusive the boolean indicating whether or not I am exclusive
   * @param isAutoDeleted the boolean indicating whether or not I should be auto-deleted
   */
  BrokerQueue(
          final ConnectionSettings connectionSettings,
          final String exchangeName,
          final String queueName,
          final boolean isDurable,
          final boolean isExclusive,
          final boolean isAutoDeleted) {

    this.connection = new BrokerConnection(connectionSettings, Type.Queue, queueName, isDurable);
    this.exchangeName = exchangeName;
    this.queueName = queueName;
    this.forwarder = new Forwarder();

    try {
      final Channel channel = connection.channel();
      final DeclareOk ok = channel.queueDeclare(queueName, isDurable, isExclusive, isAutoDeleted, null);
      channel.queueBind(ok.getQueue(), exchangeName, "");
      connection.channel().queueDeclare(queueName, isDurable, isExclusive, isAutoDeleted, null);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to create/open the queue because: " + e.getMessage(), e);
    }
  }

  /**
   * Constructs my default state.
   * @param exchange the BrokerExchange for connection and channel
   * @param queueName the String name of the queue
   */
  BrokerQueue(final BrokerExchange exchange, final String queueName) {
    this(exchange, queueName, true, false, false);
  }

  /**
   * Constructs my default state.
   * @param brokerExchange the BrokerExchange to initialize with
   * @param queueName the String name of the exchange, or the empty string
   * @param aType the String type of the exchange
   * @param isDurable the boolean indicating whether or not I am durable
   * @param isExclusive the boolean indicating whether or not I am exclusive
   * @param isAutoDeleted the boolean indicating whether or not I should be auto-deleted
   */
  BrokerQueue(
          final BrokerExchange exchange,
          final String queueName,
          final boolean isDurable,
          final boolean isExclusive,
          final boolean isAutoDeleted) {

    this.connection = exchange.connection;
    this.exchangeName = exchange.name();
    this.queueName = queueName;
    this.forwarder = new Forwarder();

    try {
      final Channel channel = exchange.connection.channel();
      channel.queueDeclare(queueName, exchange.connection.durable, false, false, null);
      channel.queueBind(queueName, exchangeName, "");
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to create/open the queue because: " + e.getMessage(), e);
    }
  }
}
