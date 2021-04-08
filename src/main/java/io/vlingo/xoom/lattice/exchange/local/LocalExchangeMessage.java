// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.local;

import java.util.Date;
import java.util.UUID;

import io.vlingo.xoom.common.message.Message;

public class LocalExchangeMessage implements Message {
  public final String id;
  public final Date occurredOn;
  public final String type;
  public final Object payload;
  public final String version;

  public LocalExchangeMessage(
          final String id,
          final String type,
          final String version,
          final Date occurredOn,
          final Object payload) {
    this.id = id;
    this.type = type;
    this.version = version;
    this.occurredOn = occurredOn;
    this.payload = payload;
  }

  public LocalExchangeMessage(final String type, final Object payload) {
    this(UUID.randomUUID().toString(), type, "1.0.0", new Date(), payload);
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T payload() {
    return (T) payload;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public Date occurredOn() {
    return occurredOn;
  }

  @Override
  public String toString() {
    return "LocalExchangeMessage[type=" + type + " payload=" + payload + "]";
  }
}
