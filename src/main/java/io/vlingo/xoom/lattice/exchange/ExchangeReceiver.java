// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

/**
 * A receiver of messages from an {@code Exchange}, which
 * may be implemented for each unique message type. The
 * {@code L} has already been mapped and adapted from
 * the exchange-typed message.
 * 
 * @param <L> the type of the local message
 */
public interface ExchangeReceiver<L> {
  /**
   * Delivers the {@code L} local typed message from the exchange to the receiver.
   * The {@code L} has already been mapped and adapted from the exchange-typed message.
   * @param message the L typed local message
   */
  void receive(final L message);
}
