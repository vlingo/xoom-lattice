package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.actors.GridAddressFactory;
import io.vlingo.actors.Returns;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.common.identity.IdentityGeneratorType;
import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.lattice.grid.hashring.MurmurSortedMapHashRing;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class GridApplicationMessageHandlerTest {

  private static final Id localNodeId = Id.of(0);
  private static final Id originalSenderNodeId = Id.of(1);

  private static final Address address = new GridAddressFactory(IdentityGeneratorType.RANDOM).unique();

  private CountDownLatch startLatch = new CountDownLatch(1);
  private CountDownLatch deliverLatch = new CountDownLatch(1);
  private CountDownLatch answerLatch = new CountDownLatch(1);
  private CountDownLatch forwardLatch = new CountDownLatch(1);
  private CountDownLatch relocateLatch = new CountDownLatch(1);
  private CountDownLatch standbyLatch = new CountDownLatch(1);

  private final GridApplicationMessageHandler handler =
      new GridApplicationMessageHandler(localNodeId, new MurmurSortedMapHashRing<Id>(100), new GridActorControl.Inbound() {
        @Override
        public <T> void start(Id receiver, Id sender, Class<T> protocol, Address address, Definition.SerializationProxy definition) {
          startLatch.countDown();
        }

        @Override
        public <T> void deliver(Id receiver, Id sender, Returns<?> returns, Class<T> protocol, Address address, Definition.SerializationProxy definition, SerializableConsumer<T> consumer, String representation) {
          if (sender.equals(localNodeId))
            deliverLatch.countDown();
          else if (sender.equals(originalSenderNodeId)){
            forwardLatch.countDown();
          }
        }

        @Override
        public <T> void answer(Id receiver, Id sender, Answer<T> answer) {
          answerLatch.countDown();
        }

        @Override
        public void forward(Id receiver, Id sender, Message message) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void relocate(Id receiver, Id sender, Definition.SerializationProxy definition, Address address, Object snapshot, List<? extends io.vlingo.actors.Message> pending) {
          relocateLatch.countDown();
        }

        @Override
        public <T> void standby(Id recipient, Id sender, Class<T> protocol, Definition.SerializationProxy definitionProxy, Address address) {
          standbyLatch.countDown();
        }
      }, null);


  @Test
  public void testStart() throws IOException, InterruptedException {
    test(from(new Start<>(null, address, null)), startLatch);
  }

  @Test
  public void testDeliver() throws IOException, InterruptedException {
    test(from(new Deliver<>(null, address, null, (something) -> {}, null)), deliverLatch);
  }

  @Test
  public void testAnswer() throws IOException, InterruptedException {
    test(from(new Answer<>(null, null)), answerLatch);
  }

  @Test
  public void testForward() throws IOException, InterruptedException {
    test(from(new Forward(originalSenderNodeId, new Deliver<>(null, address, null, (some) -> {}, null))), forwardLatch);
  }

  @Test
  public void testRelocate() throws IOException, InterruptedException {
    test(from(new Relocate(address, null, null, Collections.emptyList())), relocateLatch);
  }

  @Test
  public void testStandby() throws IOException, InterruptedException {
    test(from(new Standby<>(null, address, null)), standbyLatch);
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
    RawMessage raw = RawMessage.from(localNodeId.value(), -1, buffer.limit());
    raw.putRemaining(buffer);
    return raw;
  }

}
