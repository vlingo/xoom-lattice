package io.vlingo.lattice.exchange.local;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.message.AsyncMessageQueue;
import io.vlingo.common.message.MessageQueue;
import io.vlingo.lattice.exchange.Covey;
import io.vlingo.lattice.exchange.Exchange;
import io.vlingo.lattice.exchange.ExternalType1;
import io.vlingo.lattice.exchange.ExternalType2;
import io.vlingo.lattice.exchange.LocalType1;
import io.vlingo.lattice.exchange.LocalType2;
import io.vlingo.lattice.exchange.TestExchangeReceiver1;
import io.vlingo.lattice.exchange.TestExchangeReceiver2;

public class LocalExchangeTest {

  @Test
  public void testThatExchangeSendsTyped() {
    final TestUntil until = TestUntil.happenings(2);
    final ConcurrentLinkedQueue<Object> results = new ConcurrentLinkedQueue<>();

    final MessageQueue queue = new AsyncMessageQueue(null);
    final Exchange exchange = new LocalExchange(queue);

    exchange
      .register(Covey.of(
              new LocalExchangeSender(queue),
              new TestExchangeReceiver1(until, results),
              new LocalExchangeAdapter<LocalType1,ExternalType1>(LocalType1.class),
              LocalType1.class,
              ExternalType1.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              new LocalExchangeSender(queue),
              new TestExchangeReceiver2(until, results),
              new LocalExchangeAdapter<LocalType2,ExternalType2>(LocalType2.class),
              LocalType2.class,
              ExternalType2.class,
              LocalExchangeMessage.class));

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
