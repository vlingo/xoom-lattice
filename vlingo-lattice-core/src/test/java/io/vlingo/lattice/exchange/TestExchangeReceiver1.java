// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.lattice.exchange.ExchangeReceiver;

public class TestExchangeReceiver1 implements ExchangeReceiver<LocalType1> {
  private AccessSafely access = AccessSafely.afterCompleting(0);
  private final ConcurrentLinkedQueue<Object> results;

  public TestExchangeReceiver1(final ConcurrentLinkedQueue<Object> results) {
    this.results = results;
    this.access = AccessSafely.afterCompleting(0);
  }

  @Override
  public void receive(final LocalType1 message) {
    System.out.println("TestExchangeReceiver1 receiving: " + message);
    access.writeUsing("addMessage", message);
  }

  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely.afterCompleting(times);
    access
      .writingWith("addMessage", (LocalType1 message) -> results.add(message))
      .readingWith("getMessage", () -> results.poll());

    return access;
  }

}
