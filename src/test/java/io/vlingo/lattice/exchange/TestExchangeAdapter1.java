// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import io.vlingo.common.serialization.JsonSerialization;

public class TestExchangeAdapter1 implements ExchangeAdapter<LocalType1,ExternalType1,ExchangeMessage> {
  private ExchangeMapper<LocalType1,ExternalType1> mapper = new TestExchangeMapper1();

  @Override
  public LocalType1 fromExchange(final ExchangeMessage externalMessage) {
    final ExternalType1 external = JsonSerialization.deserialized(externalMessage.payload, ExternalType1.class);
    final LocalType1 local = mapper.externalToLocal(external);
    return local;
  }

  @Override
  public ExchangeMessage toExchange(final LocalType1 localMessage) {
    final ExternalType1 external = mapper.localToExternal(localMessage);
    final String payload = JsonSerialization.serialized(external);
    return new ExchangeMessage(ExternalType1.class.getName(), payload);
  }

  @Override
  public boolean supports(final Object exchangeMessage) {
    if (ExchangeMessage.class != exchangeMessage.getClass()) {
      return false;
    }
    return ExternalType1.class.getName().equals(((ExchangeMessage) exchangeMessage).type);
  }
}
