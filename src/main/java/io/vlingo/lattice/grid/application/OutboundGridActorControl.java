package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.lattice.grid.application.message.serialization.JavaObjectEncoder;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

import java.nio.ByteBuffer;

public class OutboundGridActorControl implements GridActorControl.Outbound {

  private final Id localNodeId;
  private ApplicationOutboundStream stream;
  private final Encoder encoder;

  public OutboundGridActorControl(Id localNodeId) {
    this(localNodeId, null, new JavaObjectEncoder());
  }

  public OutboundGridActorControl(Id localNodeId, Encoder encoder) {
    this(localNodeId, null, encoder);
  }

  public OutboundGridActorControl(Id localNodeId, ApplicationOutboundStream stream) {
    this(localNodeId, stream, new JavaObjectEncoder());
  }

  public OutboundGridActorControl(Id localNodeId, ApplicationOutboundStream stream, Encoder encoder) {
    this.localNodeId = localNodeId;
    this.stream = stream;
    this.encoder = encoder;
  }

  public void setStream(ApplicationOutboundStream outbound) {
    this.stream = outbound;
  }

  @Override
  public <T> void start(
      Id receiver,
      Id sender,
      Class<T> protocol,
      Address address,
      Class<? extends Actor> type,
      Object[] parameters) {
    send(receiver, new Start<>(protocol, address, type, parameters));
  }

  private void send(Id recipient, Message message) {
    byte[] payload = encoder.encode(message);
    RawMessage raw = RawMessage.from(
        localNodeId.value(), -1, payload.length);
    raw.putRemaining(ByteBuffer.wrap(payload));
    stream.sendTo(raw, recipient);
  }

  @Override
  public <T> void deliver(
      Id receiver,
      Id sender,
      Class<T> protocol,
      Address address,
      SerializableConsumer<T> consumer,
      String representation) {
    send(receiver, new Deliver<T>(protocol, address, consumer, representation));
  }

  @Override
  public void answer(Id receiver, Id ref, Answer answer) {
    send(receiver, answer);
  }

  @Override
  public void forward(Id receiver, Id sender, Message message) {
    send(receiver, new Forward(sender, message));
  }
}
