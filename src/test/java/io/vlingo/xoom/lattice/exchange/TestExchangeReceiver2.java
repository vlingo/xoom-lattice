// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.vlingo.xoom.actors.testkit.AccessSafely;

public class TestExchangeReceiver2 implements ExchangeReceiver<LocalType2> {
  private AccessSafely access = AccessSafely.afterCompleting(0);
  private final ConcurrentLinkedQueue<Object> results;

  public TestExchangeReceiver2(final ConcurrentLinkedQueue<Object> results) {
    this.results = results;
    this.access = AccessSafely.afterCompleting(0);
  }

  @Override
  public void receive(final LocalType2 message) {
    System.out.println("TestExchangeReceiver2 receiving: " + message);
    access.writeUsing("addMessage", message);
  }

  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely.afterCompleting(times);
    access
     .writingWith("addMessage", (LocalType2 message) -> results.add(message))
     .readingWith("getMessage", () -> results.poll());

    return access;
  }

}
