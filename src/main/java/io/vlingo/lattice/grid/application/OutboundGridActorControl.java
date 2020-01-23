package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.actors.LocalMessage;
import io.vlingo.cluster.model.CommunicationsHub;
import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

import java.io.*;
import java.nio.ByteBuffer;

public class OutboundGridActorControl implements GridActorControl.Outbound {

  private final CommunicationsHub hub;
  private final JavaObjectEncoder encoder;

  public OutboundGridActorControl(CommunicationsHub hub) {
    this.hub = hub;
    this.encoder = new JavaObjectEncoder();
  }

  @Override
  public <T> void start(Id host, Id ref, Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters) {
    send(ref, host, new Start<>(protocol, address, type, parameters));
  }

  private void send(Id sender, Id recipient, Message message) {
    byte[] payload = encoder.encode(message);
    RawMessage raw = RawMessage.from(
        sender.value(), -1, payload.length);
    raw.putRemaining(ByteBuffer.wrap(payload));
    hub.applicationOutboundStream()
        .sendTo(raw, recipient);
  }

  @Override
  public <T> void deliver(Id host, Id ref, Class<T> protocol, Address address, String representation) {
    send(ref, host, new Deliver<>(protocol, address, representation));
  }

  @Override
  public void answer(Id host, Id ref, Answer answer) {
    send(ref, host, answer);
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
