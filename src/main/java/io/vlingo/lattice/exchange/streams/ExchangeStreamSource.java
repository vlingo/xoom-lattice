// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.streams;

import io.vlingo.lattice.exchange.Exchange;
import io.vlingo.reactivestreams.operator.QueueSource;

/**
 * {@link Exchange} specific {@link QueueSource} implementation.
 *
 * @param <T> Type of the message.
 */
class ExchangeStreamSource<T> extends QueueSource<T> {
    public ExchangeStreamSource(boolean slow) {
        super(slow);
    }

    @Override
    protected void add(T value) {
        super.add(value);
    }
}
