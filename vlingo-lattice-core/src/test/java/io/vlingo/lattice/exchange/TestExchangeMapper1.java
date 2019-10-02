// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import io.vlingo.lattice.exchange.ExchangeMapper;

public class TestExchangeMapper1 implements ExchangeMapper<LocalType1,ExternalType1> {

  @Override
  public ExternalType1 localToExternal(final LocalType1 local) {
    return new ExternalType1(local.attribute1, local.attribute2);
  }

  @Override
  public LocalType1 externalToLocal(final ExternalType1 external) {
    return new LocalType1(external.field1, Integer.parseInt(external.field2));
  }
}
