// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

/**
 * A message listener, that is given each message received by a MessageConsumer.
 * I am also an adapter because I provide defaults for both handleMessage()
 * behaviors. A typical subclass would override one or the other handleMessage()
 * based on its type and leave the remaining handleMessage() defaulted since it
 * will never be used by MessageConsumer.
 */
interface MessageListener {

  /**
   * Handle a message. If any ExchangeException is thrown by my extender
   * its isRetry() is examined and, if true, the message being handled will be
   * nack'd and re-queued. Otherwise, if its isRetry() is false the message will
   * be rejected/failed (not re-queued). If any other Exception is thrown the
   * message will be considered not handled and is rejected/failed.
   * @param message the Message containing the RabbitMQ exchange message data
   * @throws Exception when any problem occurs and the message must not be acknowledged
   */
  public abstract void handleMessage(final Message message) throws Exception;
}
