// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application;

import io.vlingo.xoom.actors.*;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.application.message.Message;
import io.vlingo.xoom.lattice.grid.application.message.*;
import io.vlingo.xoom.lattice.grid.application.message.serialization.FSTEncoder;
import io.vlingo.xoom.lattice.util.OutBuffers;
import io.vlingo.xoom.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.xoom.wire.message.RawMessage;
import io.vlingo.xoom.wire.node.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class OutboundGridActorControl extends Actor implements GridActorControl.Outbound {

  private static final Logger logger = LoggerFactory.getLogger(OutboundGridActorControl.class);

  private final Id localNodeId;
  private ApplicationOutboundStream stream;
  private final Encoder encoder;
  private final BiConsumer<UUID, UnAckMessage> correlation;

  private final OutBuffers outBuffers;


  public OutboundGridActorControl(
          final Id localNodeId,
          final Encoder encoder,
          final BiConsumer<UUID, UnAckMessage> correlation,
          final OutBuffers outBuffers) {

    this(localNodeId, null, encoder, correlation, outBuffers);
  }

  public OutboundGridActorControl(
          final Id localNodeId,
          final ApplicationOutboundStream stream,
          final Encoder encoder,
          final BiConsumer<UUID, UnAckMessage> correlation,
          final OutBuffers outBuffers) {

    this.localNodeId = localNodeId;
    this.stream = stream;
    this.encoder = encoder;
    this.correlation = correlation;
    this.outBuffers = outBuffers;
  }

  @Override
  public void disburse(final Id id) {
    final Queue<Runnable> buffer = outBuffers.queue(id);
    logger.debug("Disbursing buffered messages");
    Runnable next;
    do {
      next = buffer.poll();
      if (next != null) {
        next.run();
      }
    } while (next != null);
  }

  private void send(final Id recipient, final Message message) {
    logger.debug("Buffering message {} to {}", message, recipient);
    outBuffers.enqueue(recipient, () -> {
      logger.debug("Sending message {} to {}", message, recipient);
      byte[] payload = encoder.encode(message);
      RawMessage raw = RawMessage.from(
          localNodeId.value(), -1, payload.length);
      raw.putRemaining(ByteBuffer.wrap(payload));
      stream.sendTo(raw, recipient);
    });
  }

  @Override
  public <T> void start(
          final Id recipient,
          final Id sender,
          final Class<T> protocol,
          final Address address,
          final Definition.SerializationProxy definitionProxy) {

    send(recipient, new Start<>(protocol, address, definitionProxy));
  }

  @Override
  public <T> void deliver(
          final Id recipient,
          final Id sender,
          final Returns<?> returns,
          final Class<T> protocol,
          final Address address,
          final Definition.SerializationProxy definitionProxy,
          final SerializableConsumer<T> consumer,
          final String representation) {

    final Deliver<T> deliver;
    if (returns == null) {
      deliver = new Deliver<>(protocol, address, definitionProxy, consumer, representation);
    } else {
      final UUID answerCorrelationId = UUID.randomUUID();
      deliver = new Deliver<>(protocol, address, definitionProxy, consumer, answerCorrelationId, representation);
      correlation.accept(answerCorrelationId, new UnAckMessage(recipient, returns, deliver));
    }
    send(recipient, deliver);
  }

  @Override
  public <T> void answer(final Id receiver, final Id sender, final Answer<T> answer) {
    send(receiver, answer);
  }

  @Override
  public void forward(final Id receiver, final Id sender, final Message message) {
    send(receiver, new Forward(sender, message));
  }

  @Override
  public void relocate(
          final Id receiver,
          final Id sender,
          final Definition.SerializationProxy definitionProxy,
          final Address address,
          final Object snapshot, List<? extends io.vlingo.xoom.actors.Message> pending) {

    final List<Deliver<?>> messages =
            pending
              .stream()
              .map(Deliver.from(correlation, receiver))
              .collect(Collectors.toList());

    send(receiver, new Relocate(address, definitionProxy, snapshot, messages));
  }

  @Override
  public void useStream(ApplicationOutboundStream outbound) {
    this.stream = outbound;
  }

  public static class OutboundGridActorControlInstantiator implements ActorInstantiator<OutboundGridActorControl> {
    private static final long serialVersionUID = 8987209018742138417L;

    private final Id id;
    private final FSTEncoder fstEncoder;
    private final BiConsumer<UUID, UnAckMessage> correlation;
    private final OutBuffers outBuffers;

    public OutboundGridActorControlInstantiator(
            final Id id,
            final FSTEncoder fstEncoder,
            final BiConsumer<UUID, UnAckMessage> correlation,
            final OutBuffers outBuffers) {
      this.id = id;
      this.fstEncoder = fstEncoder;
      this.correlation = correlation;
      this.outBuffers = outBuffers;
    }

    @Override
    public OutboundGridActorControl instantiate() {
      return new OutboundGridActorControl(id, fstEncoder, correlation, outBuffers);
    }
  }
}
