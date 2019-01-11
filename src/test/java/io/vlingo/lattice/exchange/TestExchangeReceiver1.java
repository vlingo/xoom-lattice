// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.lattice.exchange.ExchangeReceiver;

public class TestExchangeReceiver1 implements ExchangeReceiver<LocalType1> {
  private final TestUntil until;
  private final ConcurrentLinkedQueue<Object> results;

  public TestExchangeReceiver1(final TestUntil until, final ConcurrentLinkedQueue<Object> results) {
    this.until = until;
    this.results = results;
  }

  @Override
  public void receive(final LocalType1 message) {
    System.out.println("TestExchangeReceiver1 receiving: " + message);
    results.add(message);
    until.happened();
  }
}
