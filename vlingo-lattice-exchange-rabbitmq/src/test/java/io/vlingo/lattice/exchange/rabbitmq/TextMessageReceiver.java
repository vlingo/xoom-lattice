// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.lattice.exchange.ExchangeReceiver;

public class TextMessageReceiver implements ExchangeReceiver<String> {
  private final Object lock;
  private final ConcurrentLinkedQueue<Object> results;
  private final TestUntil until;

  public TextMessageReceiver(final TestUntil until, final ConcurrentLinkedQueue<Object> results) {
    this.until = until;
    this.results = results;
    this.lock = new Object();
  }

  @Override
  public void receive(final String message) {
    synchronized (lock) {
      results.add(message);
      until.happened();
    }
  }
}
