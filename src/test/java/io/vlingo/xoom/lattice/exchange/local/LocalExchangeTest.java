package io.vlingo.xoom.lattice.exchange.local;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.message.AsyncMessageQueue;
import io.vlingo.xoom.common.message.MessageQueue;
import io.vlingo.xoom.lattice.exchange.Covey;
import io.vlingo.xoom.lattice.exchange.Exchange;
import io.vlingo.xoom.lattice.exchange.ExternalType1;
import io.vlingo.xoom.lattice.exchange.ExternalType2;
import io.vlingo.xoom.lattice.exchange.LocalType1;
import io.vlingo.xoom.lattice.exchange.LocalType2;
import io.vlingo.xoom.lattice.exchange.TestExchangeReceiver1;
import io.vlingo.xoom.lattice.exchange.TestExchangeReceiver2;

public class LocalExchangeTest {

  @Test
  public void testThatExchangeSendsTyped() {
    final ConcurrentLinkedQueue<Object> results = new ConcurrentLinkedQueue<>();

    final MessageQueue queue = new AsyncMessageQueue(null);
    final Exchange exchange = new LocalExchange(queue);

    final TestExchangeReceiver1 receiver1 = new TestExchangeReceiver1(results);
    final AccessSafely access1 = receiver1.afterCompleting(1);

    final TestExchangeReceiver2 receiver2 = new TestExchangeReceiver2(results);
    final AccessSafely access2 = receiver2.afterCompleting(1);

    exchange
      .register(Covey.of(
              new LocalExchangeSender(queue),
              receiver1,
              new LocalExchangeAdapter<LocalType1,ExternalType1>(LocalType1.class),
              LocalType1.class,
              ExternalType1.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              new LocalExchangeSender(queue),
              receiver2,
              new LocalExchangeAdapter<LocalType2,ExternalType2>(LocalType2.class),
              LocalType2.class,
              ExternalType2.class,
              LocalExchangeMessage.class));

    final LocalType1 local1 = new LocalType1("ABC", 123);
    exchange.send(local1);

    final LocalType2 local2 = new LocalType2("DEF", 456);
    exchange.send(local2);

    assertEquals(local1, access1.readFrom("getMessage"));
    assertEquals(local2, access2.readFrom("getMessage"));

    exchange.close();
  }
}
