package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.*;
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

  CountDownLatch startLatch = new CountDownLatch(1);
  CountDownLatch deliverLatch = new CountDownLatch(1);
  CountDownLatch answerLatch = new CountDownLatch(1);
  CountDownLatch forwardLatch = new CountDownLatch(1);

  private final GridActorControlMessageHandler handler =
      new GridActorControlMessageHandler(new GridActorControl.Inbound() {
        @Override
        public <T> void start(Id recipient, Id sender, Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters) {
          startLatch.countDown();
        }

        @Override
        public <T> void deliver(Id recipient, Id sender, Class<T> protocol, Address address, SerializableConsumer<T> consumer, String representation) {
          if (sender.equals(Id.of(0)))
            deliverLatch.countDown();
          else if (sender.equals(Id.of(1))){
            forwardLatch.countDown();
          }
        }

        @Override
        public void answer(Id recipient, Id sender, Answer answer) {
          answerLatch.countDown();
        }

        @Override
        public void forward(Id recipient, Id sender, Message message) {
          throw new UnsupportedOperationException();
        }
      }, null);


  @Test
  public void testStart() throws IOException, InterruptedException {
    test(from(new Start<>(null, null, null, null)), startLatch);
  }

  @Test
  public void testDeliver() throws IOException, InterruptedException {
    test(from(new Deliver<>(null, null, (something) -> {}, null)), deliverLatch);
  }

  @Test
  public void testAnswer() throws IOException, InterruptedException {
    test(from(new Answer()), answerLatch);
  }

  @Test
  public void testForward() throws IOException, InterruptedException {
    test(from(new Forward(Id.of(1), new Deliver<>(null, null, (some) -> {}, null))), forwardLatch);
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
    RawMessage raw = RawMessage.from(0, -1, buffer.limit());
    raw.putRemaining(buffer);
    return raw;
  }

}
