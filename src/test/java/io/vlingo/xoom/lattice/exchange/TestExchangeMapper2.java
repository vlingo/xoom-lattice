// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

public class TestExchangeMapper2 implements ExchangeMapper<LocalType2,ExternalType2> {

  @Override
  public ExternalType2 localToExternal(final LocalType2 local) {
    return new ExternalType2(local.attribute1, local.attribute2);
  }

  @Override
  public LocalType2 externalToLocal(final ExternalType2 external) {
    return new LocalType2(external.field1, Integer.parseInt(external.field2));
  }
}
