// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

/**
 * Adapts the local messages of type {@code L} to exchange messages
 * of type {@code EX} that hold external type {@code E}. This may involve
 * mapping, in which case the underlying implementation must arrange a for
 * {@code ExchangeMapper<L,E>} to be established. Note that the {@code L}
 * and {@code E} types may be different between {@code ChannelAdapter<L,E>}
 * and {@code ExchangeMapper<L,E>}.
 *
 * @param <L> the local object type
 * @param <E> the external object type
 * @param <EX> the exchange message type
 */
public interface ExchangeAdapter<L,E,EX> {
  /**
   * Answer the {@code L} typed local message from the {@code exchangeMessage}
   * of type {@code EX}.
   * @param exchangeMessage the E typed exchange message
   * @return L
   */
  L fromExchange(final EX exchangeMessage);

  /**
   * Answer the {@code EX} typed exchange message from the {@code localMessage}
   * of type {@code L}.
   * @param localMessage the L typed local message
   * @return EX
   */
  EX toExchange(final L localMessage);

  /**
   * Answer whether or not this adapter supports the {@code exchangeMessage}.
   * @param exchangeMessage the possibly supported exchange message
   * @return boolean
   */
  boolean supports(final Object exchangeMessage);
}
