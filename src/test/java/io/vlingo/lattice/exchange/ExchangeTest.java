// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.message.AsyncMessageQueue;
import io.vlingo.common.message.MessageQueue;

public class ExchangeTest {

  @Test
  public void testThatExchangeSendsTyped() {
    final TestUntil until = TestUntil.happenings(2);
    final ConcurrentLinkedQueue<Object> results = new ConcurrentLinkedQueue<>();

    final MessageQueue queue = new AsyncMessageQueue(null);
    final Exchange exchange = new TestExchange(queue);

    exchange
      .register(Covey.of(
              new TestExchangeSender(queue),
              new TestExchangeReceiver1(until, results),
              new TestExchangeAdapter1(),
              LocalType1.class,
              ExternalType1.class,
              ExchangeMessage.class))
      .register(Covey.of(
              new TestExchangeSender(queue),
              new TestExchangeReceiver2(until, results),
              new TestExchangeAdapter2(),
              LocalType2.class,
              ExternalType2.class,
              ExchangeMessage.class));

    final LocalType1 local1 = new LocalType1("ABC", 123);
    exchange.send(local1);

    final LocalType2 local2 = new LocalType2("DEF", 456);
    exchange.send(local2);

    until.completes();

    assertEquals(2, results.size());
    assertEquals(local1, results.poll());
    assertEquals(local2, results.poll());

    exchange.close();
  }
}
