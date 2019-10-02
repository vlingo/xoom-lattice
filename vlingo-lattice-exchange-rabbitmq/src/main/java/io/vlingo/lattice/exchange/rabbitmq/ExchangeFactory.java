// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import io.vlingo.lattice.exchange.ConnectionSettings;

/**
 * A factory that produces RabbitMQ {@code Exchange} instances.
 */
public class ExchangeFactory {

  /**
   * Answers a new instance of a direct Exchange with the name name. The
   * underlying exchange has the isDurable quality, and is not auto-deleted.
   * @param connectionSettings the ConnectionSettings
   * @param name the String name of the exchange
   * @param isDurable the boolean indicating whether or not I am durable
   * @return BrokerExchange
   */
  public static BrokerExchange directInstance(
          final ConnectionSettings connectionSettings,
          final String name,
          final boolean isDurable) {

    return new BrokerExchange(connectionSettings, name, "direct", isDurable);
  }

  /**
   * Answers a new instance of a fan-out Exchange with the name name. The
   * underlying exchange has the isDurable quality, and is not auto-deleted.
   * @param connectionSettings the ConnectionSettings
   * @param name the String name of the exchange
   * @param isDurable the boolean indicating whether or not I am durable
   * @return BrokerExchange
   */
  public static BrokerExchange fanOutInstance(
          final ConnectionSettings connectionSettings,
          final String name,
          final boolean isDurable) {

    return new BrokerExchange(connectionSettings, name, "fanout", isDurable);
  }

  /**
   * Answers a new instance of a headers Exchange with the name name. The
   * underlying exchange has the isDurable quality, and is not auto-deleted.
   * @param connectionSettings the ConnectionSettings
   * @param name the String name of the exchange
   * @param isDurable the boolean indicating whether or not I am durable
   * @return BrokerExchange
   */
  public static BrokerExchange headersInstance(
          final ConnectionSettings connectionSettings,
          final String name,
          final boolean isDurable) {

    return new BrokerExchange(connectionSettings, name, "headers", isDurable);
  }

  /**
   * Answers a new instance of a topic Exchange with the name name. The
   * underlying exchange has the isDurable quality, and is not auto-deleted.
   * @param connectionSettings the ConnectionSettings
   * @param name the String name of the exchange
   * @param isDurable the boolean indicating whether or not I am durable
   * @return BrokerExchange
   */
  public static BrokerExchange topicInstance(
          final ConnectionSettings connectionSettings,
          final String name,
          final boolean isDurable) {

    return new BrokerExchange(connectionSettings, name, "topic", isDurable);
  }
}
