package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Returns;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.lattice.grid.application.message.serialization.JavaObjectEncoder;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OutboundGridActorControl implements GridActorControl.Outbound {

  private static final Logger logger = LoggerFactory.getLogger(OutboundGridActorControl.class);

  private final Id localNodeId;
  private ApplicationOutboundStream stream;
  private final Encoder encoder;
  private final BiConsumer<UUID, Returns<?>> correlation;

  public OutboundGridActorControl(Id localNodeId, BiConsumer<UUID, Returns<?>> correlation) {
    this(localNodeId, null, new JavaObjectEncoder(), correlation);
  }

  public OutboundGridActorControl(Id localNodeId, Encoder encoder, BiConsumer<UUID, Returns<?>> correlation) {
    this(localNodeId, null, encoder, correlation);
  }

  public OutboundGridActorControl(Id localNodeId, ApplicationOutboundStream stream, BiConsumer<UUID, Returns<?>> correlation) {
    this(localNodeId, stream, new JavaObjectEncoder(), correlation);
  }

  public OutboundGridActorControl(Id localNodeId, ApplicationOutboundStream stream, Encoder encoder, BiConsumer<UUID, Returns<?>> correlation) {
    this.localNodeId = localNodeId;
    this.stream = stream;
    this.encoder = encoder;
    this.correlation = correlation;
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
    logger.debug("Sending message {} to {}", message, recipient);
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
      Returns<?> returns,
      Class<T> protocol,
      Address address,
      Class<? extends Actor> type, SerializableConsumer<T> consumer,
      String representation) {
    final Deliver<T> deliver;
    if (returns == null) {
      deliver = new Deliver<>(protocol, address, type, consumer, representation);
    } else {
      final UUID answerCorrelationId = UUID.randomUUID();
      deliver = new Deliver<>(protocol, address, type, consumer, answerCorrelationId, representation);
      correlation.accept(answerCorrelationId, returns);
    }
    send(receiver, deliver);
  }

  @Override
  public <T> void answer(Id receiver, Id sender, Answer<T> answer) {
    send(receiver, answer);
  }

  @Override
  public void forward(Id receiver, Id sender, Message message) {
    send(receiver, new Forward(sender, message));
  }

  @Override
  public void relocate(Id receiver, Id sender, Class<? extends Actor> type, Address address, Serializable snapshot, List<? extends io.vlingo.actors.Message> pending) {
    List<Deliver<?>> messages = pending.stream()
        .map(Deliver.from(correlation))
        .collect(Collectors.toList());
    send(receiver, new Relocate(type, address, snapshot, messages));
  }
}
