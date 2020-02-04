package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class OutboundGridActorControl implements GridActorControl.Outbound {

  private final ApplicationOutboundStream outbound;
  private final JavaObjectEncoder encoder;

  public OutboundGridActorControl(ApplicationOutboundStream outbound) {
    this.outbound = outbound;
    this.encoder = new JavaObjectEncoder();
  }

  @Override
  public <T> void start(
      Id recipient,
      Id sender,
      Class<T> protocol,
      Address address,
      Class<? extends Actor> type,
      Object[] parameters) {
    send(recipient, sender, new Start<>(protocol, address, type, parameters));
  }

  private void send(Id recipient, Id sender, Message message) {
    byte[] payload = encoder.encode(message);
    RawMessage raw = RawMessage.from(
        sender.value(), -1, payload.length);
    raw.putRemaining(ByteBuffer.wrap(payload));
    outbound.sendTo(raw, recipient);
  }

  @Override
  public <T> void deliver(
      Id recipient,
      Id sender,
      Class<T> protocol,
      Address address,
      SerializableConsumer<T> consumer,
      String representation) {
    send(recipient, sender, new Deliver<T>(protocol, address, consumer, representation));
  }

  @Override
  public void answer(Id host, Id ref, Answer answer) {
    send(host, ref, answer);
  }

  @Override
  public void forward(Id recipient, Id sender, Message message) {
    throw new UnsupportedOperationException();
  }


  private static final class JavaObjectEncoder implements Encoder {

    @Override
    public byte[] encode(Message message) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
        out.writeObject(message);
        out.flush();
        return bos.toByteArray();
      } catch (IOException e) {
        throw new RuntimeException("encode failed", e);
      }
    }
  }
}
