// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import java.io.IOException;

import io.vlingo.lattice.exchange.ConnectionSettings;
import io.vlingo.lattice.exchange.Queue;

public class QueueFactory {

  /**
   * Answers a new instance of a Queue with the name name. The underlying
   * queue is non-durable, non-exclusive, and not auto-deleted.
   * @param connectionSettings the ConnectionSettings
   * @param exchangeName the String name of the exchange
   * @param queueName the String name of the queue
   * @return Queue
   */
  public static Queue instance(final ConnectionSettings connectionSettings, final String exchangeName, final String queueName) {
    return new BrokerQueue(connectionSettings, exchangeName, queueName, false, false, false);
  }

  /**
   * Answers a new instance of a Queue with the name name. The underlying
   * queue durability, exclusivity, and deletion properties are specified by
   * explicit parameters.
   * @param connectionSettings the ConnectionSettings
   * @param exchangeName the String name of the exchange
   * @param queueName the String name of the queue
   * @param isDurable the boolean indicating whether or not I am durable
   * @param isExclusive the boolean indicating whether or not I am exclusive
   * @param isAutoDeleted the boolean indicating whether or not I should be auto-deleted
   * @return Queue
   */
  public static Queue instance(
          final ConnectionSettings connectionSettings,
          final String exchangeName,
          final String queueName,
          final boolean isDurable,
          final boolean isExclusive,
          final boolean isAutoDeleted) {
    return new BrokerQueue(connectionSettings, exchangeName, queueName, isDurable, isExclusive, isAutoDeleted);
  }

  /**
   * Answers a new instance of a Queue with the name name. The underlying
   * queue is durable, is non-exclusive, and not auto-deleted.
   * @param connectionSettings the ConnectionSettings
   * @param exchangeName the String name of the exchange
   * @param queueName the String name of the queue
   * @return Queue
   */
  public static Queue durableInstance(final ConnectionSettings connectionSettings, final String exchangeName, final String queueName) {
    return new BrokerQueue(connectionSettings, exchangeName, queueName, true, false, false);
  }

  /**
   * Answers a new instance of a Queue with the name name. The underlying
   * queue is durable, exclusive, and not auto-deleted.
   * @param connectionSettings the ConnectionSettings
   * @param exchangeName the String name of the exchange
   * @param queueName the String name of the queue
   * @return Queue
   */
  public static Queue durableExclsuiveInstance(final ConnectionSettings connectionSettings, final String exchangeName, final String queueName) {
    return new BrokerQueue(connectionSettings, exchangeName, queueName, true, true, false);
  }

  /**
   * Answers a new instance of a Queue that is bound to exchange, and
   * is ready to participate as an exchange subscriber (pub/sub). The
   * connection and channel of exchange are reused. The BrokerQueue is
   * uniquely named by the server, non-durable, exclusive, and auto-deleted.
   * This BrokerQueue style best works as a temporary fan-out subscriber.
   * @param exchange the BrokerQueue to bind with the new BrokerQueue
   * @return Queue
   */
  public static Queue exchangeSubscriberInstance(final BrokerExchange exchange) {
    final BrokerQueue queue = new BrokerQueue(exchange, "", false, true, true);

    try {
      queue.connection.channel().queueBind(queue.name(), exchange.name(), "");
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to bind the queue and exchange because: " + e.getMessage(), e);
    }

    return queue;
  }

  /**
   * Answers a new instance of a Queue that is bound to exchange, and
   * is ready to participate as an exchange subscriber (pub/sub). The
   * connection and channel of exchange are reused. The BrokerQueue is
   * uniquely named by the server, non-durable, exclusive, and auto-deleted.
   * The queue is bound to all routing keys in routingKeys. This BrokerQueue
   * style best works as a temporary direct or topic subscriber.
   * @param exchange the RabbitMQExchange to bind with the new BrokerQueue
   * @param routingKeys the String[] of routingKeys
   * @return Queue
   */
  public static Queue exchangeSubscriberInstance(final BrokerExchange exchange, final String[] routingKeys) {
    final BrokerQueue queue = new BrokerQueue(exchange, "", false, true, true);

    try {
      for (String routingKey : routingKeys) {
        queue.connection.channel().queueBind(queue.name(), exchange.name(), routingKey);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to bind the queue and exchange because: " + e.getMessage(), e);
    }

    return queue;
  }

  /**
   * Answers a new instance of a Queue that is bound to exchange, and
   * is ready to participate as an exchange subscriber (pub/sub). The
   * connection and channel of exchange are reused. The BrokerQueue is named
   * by name, unless it is empty, in which case the name is generated by
   * the broker. The BrokerQueue is bound to all routing keys in routingKeys,
   * or to no routing key if routingKeys is empty. The BrokerQueue has the
   * qualities specified by isDurable, isExclusive, isAutoDeleted. This
   * factory is provided for ultimate flexibility in case no other
   * exchange-queue binder factories fit the needs of the client.
   * @param exchange the BrokerQueue to bind with the new BrokerQueue
   * @param name the String name of the queue
   * @param routingKeys the routing keys to bind the queue to
   * @param isDurable the boolean indicating whether or not I am durable
   * @param isExclusive the boolean indicating whether or not I am exclusive
   * @param isAutoDeleted the boolean indicating whether or not I should be auto-deleted
   * @return Queue
   */
  public static Queue exchangeSubscriberInstance(
          final BrokerExchange exchange,
          final String name,
          final String[] routingKeys,
          final boolean isDurable,
          final boolean isExclusive,
          final boolean isAutoDeleted) {

    final BrokerQueue queue = new BrokerQueue(exchange, name, isDurable, isExclusive, isAutoDeleted);

    try {
      if (routingKeys.length == 0) {
        queue.connection.channel().queueBind(queue.name(), exchange.name(), "");
      } else {
        for (String routingKey : routingKeys) {
          queue.connection.channel().queueBind(queue.name(), exchange.name(), routingKey);
        }
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to bind the queue and exchange because: " + e.getMessage(), e);
    }

    return queue;
  }

  /**
   * Answers a new instance of a Queue that is bound to exchange, and
   * is ready to participate as an exchange subscriber (pub/sub). The
   * connection and channel of exchange are reused. The BrokerQueue is
   * named by name, which must be provided and should be unique to the
   * individual subscriber. The BrokerQueue is durable, non-exclusive, and
   * is not auto-deleted. This BrokerQueue style best works as a durable
   * fan-out exchange subscriber.
   * @param exchange the BrokerQueue to bind with the new BrokerQueue
   * @param name the String name of the queue, which must be unique, non-empty
   * @return Queue
   */
  public static Queue individualExchangeSubscriberInstance(final BrokerExchange exchange, final String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("An individual subscriber must be named.");
    }

    final BrokerQueue queue = new BrokerQueue(exchange, name, true, false, false);

    try {
      queue.connection.channel().queueBind(queue.name(), exchange.name(), "");
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to bind the queue and exchange because: " + e.getMessage(), e);
    }
    return queue;
  }

  /**
   * Answers a new instance of a Queue that is bound to exchange, and
   * is ready to participate as an exchange subscriber (pub/sub). The
   * connection and channel of exchange are reused. The BrokerQueue is
   * by name, which must be provided and should be unique to the
   * individual subscriber. The queue is bound to all routing keys in
   * routingKeys. The BrokerQueue is durable, non-exclusive, and is not
   * auto-deleted. This BrokerQueue style best works as a durable direct or
   * topic exchange subscriber.
   * @param exchange the BrokerQueue to bind with the new BrokerQueue
   * @param name the String name of the queue, which must be unique, non-empty
   * @param routingKeys the String[] routing keys to bind the queue to
   * @return Queue
   */
  public static Queue individualExchangeSubscriberInstance(
          final BrokerExchange exchange,
          final String name,
          final String[] routingKeys) {

    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("An individual subscriber must be named.");
    }

    final BrokerQueue queue = new BrokerQueue(exchange, name, true, false, false);

    try {
      for (final String routingKey : routingKeys) {
        queue.connection.channel().queueBind(queue.connection.name, exchange.connection.name, routingKey);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to bind the queue and exchange because: " + e.getMessage(), e);
    }

    return queue;
  }
}
