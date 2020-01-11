// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.lattice.exchange.ExchangeAdapter;
import io.vlingo.lattice.exchange.ExchangeMapper;

public class TestExchangeAdapter2 implements ExchangeAdapter<LocalType2,ExternalType2,ExchangeMessage> {
  private ExchangeMapper<LocalType2,ExternalType2> mapper = new TestExchangeMapper2();

  @Override
  public LocalType2 fromExchange(final ExchangeMessage externalMessage) {
    final ExternalType2 external = JsonSerialization.deserialized(externalMessage.payload, ExternalType2.class);
    final LocalType2 local = mapper.externalToLocal(external);
    return local;
  }

  @Override
  public ExchangeMessage toExchange(final LocalType2 localMessage) {
    final ExternalType2 external = mapper.localToExternal(localMessage);
    final String payload = JsonSerialization.serialized(external);
    return new ExchangeMessage(ExternalType2.class.getName(), payload);
  }

  @Override
  public boolean supports(final Object exchangeMessage) {
    if (ExchangeMessage.class != exchangeMessage.getClass()) {
      return false;
    }
    return ExternalType2.class.getName().equals(((ExchangeMessage) exchangeMessage).type);
  }
}
