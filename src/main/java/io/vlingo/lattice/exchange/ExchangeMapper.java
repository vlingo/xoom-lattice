// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

/**
 * Supports mapping a local type to external type, and a external type to local type.
 * @param <L> the local type
 * @param <E> the external type
 */
public interface ExchangeMapper<L,E> {
  /**
   * Answer the external type given the local type.
   * @param local the L local type to map
   * @return E
   */
  E localToExternal(final L local);

  /**
   * Answer the local type given the external type.
   * @param external the E external type to map
   * @return L
   */
  L externalToLocal(final E external);
}
