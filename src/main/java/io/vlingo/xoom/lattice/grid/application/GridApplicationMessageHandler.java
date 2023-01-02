// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduler;
import io.vlingo.xoom.lattice.grid.application.message.*;
import io.vlingo.xoom.lattice.grid.application.message.serialization.JavaObjectDecoder;
import io.vlingo.xoom.lattice.grid.hashring.HashRing;
import io.vlingo.xoom.lattice.util.HardRefHolder;
import io.vlingo.xoom.lattice.util.WeakQueue;
import io.vlingo.xoom.wire.message.RawMessage;
import io.vlingo.xoom.wire.node.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class GridApplicationMessageHandler implements ApplicationMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(GridApplicationMessageHandler.class);

  private final Id localNode;
  private final AtomicBoolean isClusterHealthy = new AtomicBoolean(false);
  private final HashRing<Id> hashRing;
  private final GridActorControl.Inbound inbound;
  private final GridActorControl.Outbound outbound;
  private final Decoder decoder;
  private final Visitor visitor;
  private final Scheduler scheduler;

  private final HardRefHolder holder;
  private final Queue<Runnable> buffer = new WeakQueue<>(); // buffer messages when cluster is not healthy

  public GridApplicationMessageHandler(
      final Id localNode, final HashRing<Id> hashRing,
      final GridActorControl.Inbound inbound,
      final GridActorControl.Outbound outbound,
      final HardRefHolder holder,
      final Scheduler scheduler) {
    this(localNode, hashRing, inbound, outbound, new JavaObjectDecoder(), holder, scheduler);
  }

  public GridApplicationMessageHandler(
      final Id localNode, final HashRing<Id> hashRing,
      final GridActorControl.Inbound inbound,
      final GridActorControl.Outbound outbound,
      final Decoder decoder,
      final HardRefHolder holder,
      final Scheduler scheduler) {

    this.localNode = localNode;
    this.hashRing = hashRing;
    this.inbound = inbound;
    this.outbound = outbound;
    this.decoder = decoder;
    this.holder = holder;
    this.scheduler = scheduler;

    this.visitor = new ControlMessageVisitor();
  }

  @Override
  public void handle(final RawMessage raw) {
    try {
      final Message message = decoder.decode(raw.asBinaryMessage());
      final Id sender = Id.of(raw.header().nodeId());
      logger.debug("Buffering message {} from {}", message, sender);
      final Runnable runnable = () -> {
        logger.debug("Handling message {} from {}", message, sender);
        message.accept(localNode, sender, visitor);
      };

      if (isClusterHealthy.get()) {
        runnable.run(); // incoming messages are dispatched immediately
      } else {
        buffer.offer(runnable); // buffer messages; cluster is not healthy
      }

      if (Objects.nonNull(holder)) {
        holder.holdOnTo(runnable);
      }
    } catch (Exception e) {
      logger.error(String.format("Failed to process message %s", raw), e);
    }
  }

  @Override
  public void informClusterIsHealthy(boolean isHealthyCluster) {
    boolean oldValue =  this.isClusterHealthy.getAndSet(isHealthyCluster);
    if (isHealthyCluster && !oldValue) {
      disburse();
    }
  }

  private void disburse() {
    if (buffer.size() == 0) return;
    logger.debug("Disbursing {} buffered messages", buffer.size());
    Runnable next;
    do {
      next = buffer.poll();
      if (next != null) {
        next.run();
      }
    } while (next != null);
  }

  final class ControlMessageVisitor implements Visitor {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void visit(final Id receiver, final Id sender, final Answer answer) {
      inbound.answer(receiver, sender, answer);
    }

    @Override
    public <T> void visit(final Id receiver, final Id sender, final GridDeliver<T> gridDeliver) {
      final Id recipient = receiver(receiver, gridDeliver.address);
      if (recipient == receiver) {
        inbound.gridDeliver(
            receiver, sender,
            returnsAnswer(receiver, sender, gridDeliver.answerCorrelationId),
            gridDeliver.protocol, gridDeliver.address, gridDeliver.definition, gridDeliver.consumer, gridDeliver.representation);
      } else {
        outbound.forward(recipient, sender, gridDeliver);
      }
    }

    @Override
    public <T> void visit(Id receiver, Id sender, ActorDeliver<T> actorDeliver) {
      inbound.actorDeliver(
              receiver, sender, returnsAnswer(receiver, sender, actorDeliver.answerCorrelationId),
              actorDeliver.protocol, actorDeliver.actorProvider, actorDeliver.consumer, actorDeliver.representation);
    }

    @Override
    public <T> void visit(final Id receiver, final Id sender, final Start<T> start) {
      final Id recipient = receiver(receiver, start.address);
      if (recipient == receiver) {
        inbound.start(receiver, sender, start.protocol, start.address, start.definition);
      } else {
        outbound.forward(recipient, sender, start);
      }
    }

    private Id receiver(final Id receiver, final Address address) {
      final Id recipient = hashRing.nodeOf(address.idString());
      if (recipient == null || recipient.equals(receiver)) {
        return receiver;
      }
      return recipient;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void visit(final Id receiver, final Id sender, final Relocate relocate) {
      final Id recipient = receiver(receiver, relocate.address);
      if (recipient == receiver) {
        final List<LocalMessage> pending = relocate.pending.stream()
            .map(deliver ->
                new LocalMessage(null, deliver.protocol, deliver.consumer,
                    returnsAnswer(receiver, sender, deliver.answerCorrelationId), deliver.representation))
            .collect(Collectors.toCollection(ArrayList::new));
        inbound.relocate(receiver, sender, relocate.definition,
            relocate.address, relocate.snapshot, pending);
      } else {
        outbound.forward(recipient, sender, relocate);
      }
    }

    private Returns<?> returnsAnswer(final Id receiver, final Id sender, final UUID answerCorrelationId) {
      if (answerCorrelationId == null) {
        return null;
      }

      final Completes<Object> completes = Completes.using(scheduler);
      completes.andThen(result -> new Answer<>(answerCorrelationId, result))
              .recoverFrom(error -> new Answer<>(answerCorrelationId, error))
              .otherwise(ignored -> new Answer<>(answerCorrelationId, new TimeoutException()))
              .andThenConsume(4000,
                      answer -> outbound.answer(sender, receiver, answer))
              .andFinally();

      // 'root' Completes here! Completes#with(Object) will trigger the execution of whole Completes chain/pipeline built above
      return Returns.value(completes);
    }
  }
}
