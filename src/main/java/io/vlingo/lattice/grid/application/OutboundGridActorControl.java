package io.vlingo.lattice.grid.application;

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
  public void start(Start start) {
    send(start);
  }

  private void send(Message.Impl message) {
    byte[] payload = encoder.encode(message);
    RawMessage raw = RawMessage.from(
        message.sender, -1, payload.length);
    raw.putRemaining(ByteBuffer.wrap(payload));
    hub.applicationOutboundStream()
        .sendTo(raw, Id.of(message.recipient));
  }

  @Override
  public void deliver(Deliver deliver) {
    send(deliver);
  }

  @Override
  public void answer(Answer answer) {
    send(answer);
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
