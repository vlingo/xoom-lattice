// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.local;

import io.vlingo.common.message.Message;

public class LocalExchangeMessage implements Message {
  public final String type;
  public final Object payload;

  public LocalExchangeMessage(final String type, final Object payload) {
    this.type = type;
    this.payload = payload;
  }

  @Override
  public String toString() {
    return "LocalExchangeMessage[type=" + type + " payload=" + payload + "]";
  }
}
