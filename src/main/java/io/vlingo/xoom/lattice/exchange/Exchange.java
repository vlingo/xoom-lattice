// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

/**
 * Defines a message exchange, such as a queue or topic, through which any number of related
 * {@code ExchangeSender}, {@code ExchangeReceiver<L>}, and {@code ExchangeAdapter<L,C>} components
 * are registered, and messages are sent.
 */
public interface Exchange {

  /**
   * Close this Exchange and any underlying resources.
   */
  void close();

  /**
   * Answer my channel, which is implementation dependent.
   * @param <T> the type of the channel
   * @return T
   */
  <T> T channel();

  /**
   * Answer my connect, which is implementation dependent.
   * @param <T> the type of the connection
   * @return T
   */
  <T> T connection();

  /**
   * Answer my name.
   * @return String
   */
  String name();

  /**
   * Registers a {@code Covey<L,C,EX>} with this Exchange.
   * @param covey the {@code Covey<L,E,EX>} to register
   * @param <L> the local object type
   * @param <E> the external object type
   * @param <EX> the exchange message type
   * @return Exchange
   */
  <L,E,EX> Exchange register(final Covey<L,E,EX> covey);

  /**
   * Sends the {@code local} message to the exchange after first adapting it to a exchange message.
   * @param local the L local message to send as an exchange message
   * @param <L> the local type
   */
  <L> void send(final L local);

  /**
   * Answer whether I am active or not.
   * @return Boolean
   */
  default boolean isActive() {
    throw new UnsupportedOperationException("This method is unsupported by default");
  }
}
