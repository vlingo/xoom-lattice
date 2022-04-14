// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

/**
 * Defines a message exchange, as a queue, through which any number of related
 * {@code ExchangeSender}, {@code ExchangeReceiver<L>}, and {@code ExchangeAdapter<L,C>} components
 * are registered, and messages are sent.
 */
public interface Queue extends Exchange {

}
