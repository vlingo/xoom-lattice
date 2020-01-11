// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

/**
 * A sender of messages to a {@code Exchange}.
 *
 * @param <E> the exchange typed message
 */
public interface ExchangeSender<E> {
  /**
   * Sends the {@code E} exchange typed message through the exchange.
   * @param message the E exchange typed message to send
   */
  void send(final E message);
}
