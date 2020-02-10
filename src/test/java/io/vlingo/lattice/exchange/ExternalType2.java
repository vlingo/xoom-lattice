// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import java.util.Date;

import io.vlingo.common.message.Message;

public class ExternalType2 implements Message {
  public final String field1;
  public final String field2;

  public ExternalType2(final String value1, final int value2) {
    this.field1 = value1;
    this.field2 = Integer.toString(value2);
  }

  @Override
  public String toString() {
    return "ExternalType[field1=" + field1 + " field2=" + field2 + "]";
  }

  @Override
  public String id() {
    return null;
  }

  @Override
  public Date occurredOn() {
    return null;
  }

  @Override
  public <T> T payload() {
    return null;
  }

  @Override
  public String type() {
    return null;
  }

  @Override
  public String version() {
    return null;
  }
}
