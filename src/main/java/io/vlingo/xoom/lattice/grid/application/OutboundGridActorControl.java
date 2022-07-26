// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.cluster.model.node.Registry;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.lattice.grid.application.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.application.message.serialization.FSTEncoder;
import io.vlingo.xoom.lattice.util.OutBuffers;
import io.vlingo.xoom.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.xoom.wire.message.RawMessage;
import io.vlingo.xoom.wire.node.Id;

public class OutboundGridActorControl extends Actor implements GridActorControl.Outbound {

  private static final Logger logger = LoggerFactory.getLogger(OutboundGridActorControl.class);

  private final Id localNodeId;
  private final Registry registry;
  private ApplicationOutboundStream stream;
  private final Encoder encoder;
  private final BiConsumer<UUID, UnAckMessage> gridMessagesCorrelationConsumer;
  private final BiConsumer<UUID, Returns<?>> actorMessagesCorrelationConsumer;

  private final OutBuffers outBuffers; // buffer messages for unhealthy nodes
  private final AtomicBoolean isHealthyCluster;


  public OutboundGridActorControl(
          final Id localNodeId,
          final Registry registry,
          final Encoder encoder,
          final BiConsumer<UUID, UnAckMessage> gridMessagesCorrelationConsumer,
          final BiConsumer<UUID, Returns<?>> actorMessagesCorrelationConsumer,
          final OutBuffers outBuffers) {

    this(localNodeId, registry, null, encoder, gridMessagesCorrelationConsumer, actorMessagesCorrelationConsumer, outBuffers);
  }

  public OutboundGridActorControl(
          final Id localNodeId,
          final Registry registry,
          final ApplicationOutboundStream stream,
          final Encoder encoder,
          final BiConsumer<UUID, UnAckMessage> gridMessagesCorrelationConsumer,
          final BiConsumer<UUID, Returns<?>> actorMessagesCorrelationConsumer,
          final OutBuffers outBuffers) {

    this.localNodeId = localNodeId;
    this.registry = registry;
    this.stream = stream;
    this.encoder = encoder;
    this.gridMessagesCorrelationConsumer = gridMessagesCorrelationConsumer;
    this.actorMessagesCorrelationConsumer = actorMessagesCorrelationConsumer;
    this.outBuffers = outBuffers;
    this.isHealthyCluster = new AtomicBoolean(false);
  }

  @Override
  public void informClusterIsHealthy(boolean isHealthyCluster) {
    this.isHealthyCluster.set(isHealthyCluster);
  }

  private void send(final Id recipient, final Message message) {
    final Runnable sendFunction = () -> {
      logger.debug("Sending message {} to {}", message, recipient);
      byte[] payload = encoder.encode(message);
      RawMessage raw = RawMessage.from(
              localNodeId.value(), -1, payload.length);
      raw.putRemaining(ByteBuffer.wrap(payload));
      stream.sendTo(raw, registry.getNode(recipient));
    };

    if (isHealthyCluster.get()) {
      sendFunction.run(); // send the message immediately, node is healthy
    } else {
      logger.debug("Buffering message {} to {}", message, recipient);
      outBuffers.enqueue(recipient, sendFunction); // enqueue the message, node is unhealthy
    }
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
  public <T> void gridDeliver(
          final Id recipient,
          final Id sender,
          final Returns<?> returns,
          final Class<T> protocol,
          final Address address,
          final Definition.SerializationProxy definitionProxy,
          final SerializableConsumer<T> consumer,
          final String representation) {

    final GridDeliver<T> gridDeliver;
    if (returns == null) {
      gridDeliver = new GridDeliver<>(protocol, address, definitionProxy, consumer, representation);
    } else {
      final UUID answerCorrelationId = UUID.randomUUID();
      gridDeliver = new GridDeliver<>(protocol, address, definitionProxy, consumer, answerCorrelationId, representation);
      gridMessagesCorrelationConsumer.accept(answerCorrelationId, new UnAckMessage(recipient, returns, gridDeliver));
    }
    send(recipient, gridDeliver);
  }

  @Override
  public <T> void actorDeliver(
          Id recipient,
          Id sender,
          Returns<?> returns,
          Class<T> protocol,
          Function<Grid, Actor> actorProvider,
          SerializableConsumer<T> consumer,
          String representation) {
    final ActorDeliver<T> actorDeliver;
    if (returns == null) {
      actorDeliver = new ActorDeliver<>(protocol, actorProvider, consumer, representation);
    } else {
      final UUID answerCorrelationId = UUID.randomUUID();
      actorDeliver = new ActorDeliver<>(protocol, actorProvider, consumer, representation, answerCorrelationId);
      actorMessagesCorrelationConsumer.accept(answerCorrelationId, returns);
    }

    send(recipient, actorDeliver);
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

    final List<GridDeliver<?>> messages =
            pending
              .stream()
              .map(GridDeliver.from(gridMessagesCorrelationConsumer, receiver))
              .collect(Collectors.toList());

    send(receiver, new Relocate(address, definitionProxy, snapshot, messages));
  }

  @Override
  public void useStream(ApplicationOutboundStream outbound) {
    this.stream = outbound;
  }

  private void disburse(final Id id) {
    final Queue<Runnable> buffer = outBuffers.queue(id);
    if (buffer.size() == 0) {
      return;
    }

    logger.debug("Disbursing {} buffered messages to node {}", buffer.size(), id);
    Runnable next;
    do {
      next = buffer.poll();
      if (next != null) {
        next.run();
      }
    } while (next != null);
  }

  public static class OutboundGridActorControlInstantiator implements ActorInstantiator<OutboundGridActorControl> {
    private static final long serialVersionUID = 8987209018742138417L;

    private final Id id;
    private final Registry registry;
    private final FSTEncoder fstEncoder;
    private final BiConsumer<UUID, UnAckMessage> gridMessagesCorrelationConsumer;
    private final BiConsumer<UUID, Returns<?>> actorMessagesCorrelationConsumer;
    private final OutBuffers outBuffers;

    public OutboundGridActorControlInstantiator(
            final Id id,
            final Registry registry,
            final FSTEncoder fstEncoder,
            final BiConsumer<UUID, UnAckMessage> gridMessagesCorrelationConsumer,
            final BiConsumer<UUID, Returns<?>> actorMessagesCorrelationConsumer,
            final OutBuffers outBuffers) {
      this.id = id;
      this.registry = registry;
      this.fstEncoder = fstEncoder;
      this.gridMessagesCorrelationConsumer = gridMessagesCorrelationConsumer;
      this.actorMessagesCorrelationConsumer = actorMessagesCorrelationConsumer;
      this.outBuffers = outBuffers;
    }

    @Override
    public OutboundGridActorControl instantiate() {
      return new OutboundGridActorControl(id, registry, fstEncoder, gridMessagesCorrelationConsumer, actorMessagesCorrelationConsumer, outBuffers);
    }
  }
}
