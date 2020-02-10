package io.vlingo.lattice.grid.example;

import io.vlingo.actors.World;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GreetingTest {
  @Test
  public void testRespond() throws ExecutionException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Greeting greeting = World.start("my-world")
        .actorFor(Greeting.class, GreetingActor.class, "node");
    greeting.respond("test").andThenConsume((s) -> {
      assertEquals("Didn't match", "Hello test from node", s);
      latch.countDown();
    });
    assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
  }

  private static void respondTest(Greeting greeting, CountDownLatch latch) throws ExecutionException, InterruptedException {

  }
}
