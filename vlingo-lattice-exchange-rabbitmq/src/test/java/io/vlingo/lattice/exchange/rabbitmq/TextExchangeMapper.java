// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import io.vlingo.lattice.exchange.ExchangeMapper;

public class TextExchangeMapper implements ExchangeMapper<String,String> {

  @Override
  public String localToExternal(final String local) {
    return new String(local);
  }

  @Override
  public String externalToLocal(final String external) {
    return new String(external);
  }
}
