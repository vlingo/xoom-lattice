// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

/**
 * An abstract base class for exchange listeners performing basic set up
 * according to the answers from my concrete subclass.
 */
class ExchangeListener implements MessageListener {
  private final BrokerConnection connection;
  private final BrokerExchange exchange;
  private final MessageConsumer messageConsumer;
  private final BrokerQueue queue;

  //====================================
  // MessageListener
  //====================================

  @Override
  public void handleMessage(final Message message) throws Exception {
    exchange.forwarder().forwardToReceiver(message);
  }

  //====================================
  // ExchangeListener
  //====================================

  /**
   * Constructs my default state.
   * @param connectionSettings the ConnectionSettings to access the exchange and queue
   */
  ExchangeListener(final BrokerExchange exchange, final String queueName) {
    this.connection = exchange.connection;
    this.exchange = exchange;
    this.queue = BrokerQueue.using(exchange, queueName);
    this.messageConsumer = MessageConsumer.instance(queue, false);
    this.messageConsumer.receiveAll(this);
  }

  /**
   * Close my queue.
   */
  void close() {
    connection.close();
    messageConsumer.close();
  }

  /**
   * Answer the String name of the exchange I listen to.
   * @return String
   */
  String exchangeName() {
    return exchange.name();
  }

  /**
   * Answer the String name of the queue I listen to. By
   * default it is the simple name of my concrete class.
   * May be overridden to change the name.
   * @return String
   */
  String queueName() {
    return exchange.name() + "-queue";
  }

//
//  /**
//   * Answer the BrokerQueue for a given ConnectionSettings.
//   * @param connectionSettings ConnectionSettings
//   * @return BrokerQueue
//   */
//  private BrokerQueue attachToQueue(final ConnectionSettings connectionSettings) {
//    return queueFrom(exchangeFrom(connectionSettings));
//  }
//
//  /**
//   * Answer the BrokerQueue for a given BrokerExchange.
//   * @param exchange BrokerExchange
//   * @return BrokerQueue
//   */
//  private BrokerQueue attachToQueue(final BrokerExchange exchange) {
//    return queueFrom(exchange);
//  }
//
//  /**
//   * Answer a new BrokerExchange given the connectionSettings.
//   * @param connectionSettings the ConnectionSettings
//   * @return BrokerExchange
//   */
//  private BrokerExchange exchangeFrom(final ConnectionSettings connectionSettings) {
//    return (BrokerExchange) ExchangeFactory.fanOutInstance(connectionSettings, exchangeName(), true);
//  }
}
