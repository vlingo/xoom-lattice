// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import io.vlingo.lattice.exchange.ConnectionSettings;
import io.vlingo.lattice.exchange.Covey;
import io.vlingo.lattice.exchange.Exchange;
import io.vlingo.lattice.exchange.Forwarder;
import io.vlingo.lattice.exchange.rabbitmq.BrokerConnection.Type;

/**
 * An Exchange for RabbitMQ via a BrokerChannel.
 */
class BrokerExchange implements Exchange {
  /** My type, which is the type of exchange. */
  public final String type;

  /** My connection to the broker. */
  final BrokerConnection connection;

  /** My exchange listener, which forwards messages through my forwarder. */
  private final ExchangeListener listener;

  /** My forwarder. */
  private final Forwarder forwarder;

  /**
   * Answer my forwarder for internal use only.
   * @return Forwarder
   */
  Forwarder forwarder() {
    return forwarder;
  }

  //====================================
  // Exchange
  //====================================

  /*
   * @see io.vlingo.lattice.exchange.Exchange#close()
   */
  @Override
  public void close() {
    listener.close();

    connection.close();
  }

  /*
   * @see io.vlingo.lattice.exchange.Exchange#channel()
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T channel() {
    return (T) connection.channel();
  }

  /*
   * @see io.vlingo.lattice.exchange.Exchange#connection()
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T connection() {
    return (T) connection;
  }

  /*
   * @see io.vlingo.lattice.exchange.Exchange#name()
   */
  @Override
  public String name() {
    return connection.name;
  }

  /*
   * @see io.vlingo.lattice.exchange.Exchange#register(io.vlingo.lattice.exchange.Covey)
   */
  @Override
  public <L, E, EX> Exchange register(final Covey<L, E, EX> covey) {
    forwarder.register(covey);
    return this;
  }

  /*
   * @see io.vlingo.lattice.exchange.Exchange#send(java.lang.Object)
   */
  @Override
  public <L> void send(final L local) {
    forwarder.forwardToSender(local);
  }

  /**
   * Constructs my default state.
   * @param connectionSettings the ConnectionSettings
   * @param name the String name of the exchange
   * @param type the String type of the exchange
   * @param isDurable the boolean indicating whether or not I am durable
   */
  BrokerExchange(
          final ConnectionSettings connectionSettings,
          final String name,
          final String type,
          final boolean isDurable) {

    this.connection = new BrokerConnection(connectionSettings, Type.Exchange, name, isDurable);
    this.forwarder = new Forwarder();
    this.type = type;

    try {
      this.connection.channel().exchangeDeclare(name, type, isDurable);
      this.listener = new ExchangeListener(this, name + ".self-listening-queue");
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create/open the exchange because: " + e.getMessage(), e);
    }
  }
}
