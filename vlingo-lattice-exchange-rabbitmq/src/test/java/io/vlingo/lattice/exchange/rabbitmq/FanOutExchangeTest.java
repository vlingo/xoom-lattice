package io.vlingo.lattice.exchange.rabbitmq;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.lattice.exchange.ConnectionSettings;
import io.vlingo.lattice.exchange.Covey;
import io.vlingo.lattice.exchange.Exchange;

public class FanOutExchangeTest {

  @Test
  public void testThatFanOutExchangeHearsItself() {
    final Exchange exchange = ExchangeFactory.fanOutInstance(settings(), "test-fanout", true);

    final TestUntil until = TestUntil.happenings(2);
    final ConcurrentLinkedQueue<Object> results = new ConcurrentLinkedQueue<>();

    exchange
      .register(Covey.of(
              new MessageSender(exchange.connection()),
              new TextMessageReceiver(until, results),
              new TextExchangeAdapter(),
              String.class,
              String.class,
              Message.class));

    exchange.send("ABC");
    exchange.send("DEF");

    until.completes();

    assertEquals(2, results.size());
    assertEquals("ABC", results.poll());
    assertEquals("DEF", results.poll());

    exchange.close();
  }

  private ConnectionSettings settings() {
    return ConnectionSettings.instance("localhost", ConnectionSettings.UndefinedPort, "/", "guest", "guest");
  }
}
