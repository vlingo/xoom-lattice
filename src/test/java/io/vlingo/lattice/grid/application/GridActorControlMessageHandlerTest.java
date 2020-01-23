package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Deliver;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.lattice.grid.application.message.Start;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class GridActorControlMessageHandlerTest {

  private static final short sender = (short) 0;
  private static final short recipient = (short) 1;

  CountDownLatch startLatch = new CountDownLatch(1);
  CountDownLatch deliverLatch = new CountDownLatch(1);
  CountDownLatch answerLatch = new CountDownLatch(1);

  private final GridActorControlMessageHandler handler =
      new GridActorControlMessageHandler(new GridActorControl.Inbound() {
        @Override
        public <T> void start(Id host, Id ref, Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters) {
          startLatch.countDown();
        }

        @Override
        public <T> void deliver(Id host, Id ref, Class<T> protocol, Address address, String representation) {
          deliverLatch.countDown();
        }

        @Override
        public void answer(Id host, Id ref, Answer answer) {
          answerLatch.countDown();
        }
      });


  @Test
  public void testStart() throws IOException, InterruptedException {
    test(from(new Start<>(null, null, null, null)), startLatch);
  }

  @Test
  public void testDeliver() throws IOException, InterruptedException {
    test(from(new Deliver<>(null, null, null)), deliverLatch);
  }

  @Test
  public void testAnswer() throws IOException, InterruptedException {
    test(from(new Answer()), answerLatch);
  }

  private void test(RawMessage message, CountDownLatch latch) throws InterruptedException {
    handler.handle(message);
    assertTrue("Didn't handle " + message.getClass().getName(), latch.await(1, TimeUnit.MILLISECONDS));
  }

  private static RawMessage from(Message message) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(message);
    }
    ByteBuffer buffer = ByteBuffer.wrap(bos.toByteArray());
    RawMessage raw = RawMessage.from(1, -1, buffer.limit());
    raw.putRemaining(buffer);
    return raw;
  }

}
