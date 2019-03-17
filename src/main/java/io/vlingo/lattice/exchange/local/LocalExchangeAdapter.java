// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.local;

import io.vlingo.lattice.exchange.ExchangeAdapter;

public class LocalExchangeAdapter<L,E> implements ExchangeAdapter<L,E,LocalExchangeMessage> {
  private final Class<L> localType;

  public LocalExchangeAdapter(final Class<L> localType) {
    this.localType = localType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public L fromExchange(final LocalExchangeMessage exchangeMessage) {
    return (L) exchangeMessage.payload;
  }

  @Override
  public LocalExchangeMessage toExchange(final L localMessage) {
    return new LocalExchangeMessage(localMessage.getClass().getName(), localMessage);
  }

  @Override
  public boolean supports(final Object exchangeMessage) {
    return ((LocalExchangeMessage) exchangeMessage).payload.getClass() == localType;
  }
}
