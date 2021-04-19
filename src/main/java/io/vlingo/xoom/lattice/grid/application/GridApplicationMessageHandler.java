// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduler;
import io.vlingo.xoom.lattice.grid.application.message.Answer;
import io.vlingo.xoom.lattice.grid.application.message.Decoder;
import io.vlingo.xoom.lattice.grid.application.message.Deliver;
import io.vlingo.xoom.lattice.grid.application.message.Message;
import io.vlingo.xoom.lattice.grid.application.message.Relocate;
import io.vlingo.xoom.lattice.grid.application.message.Start;
import io.vlingo.xoom.lattice.grid.application.message.Visitor;
import io.vlingo.xoom.lattice.grid.application.message.serialization.JavaObjectDecoder;
import io.vlingo.xoom.lattice.grid.hashring.HashRing;
import io.vlingo.xoom.lattice.util.HardRefHolder;
import io.vlingo.xoom.lattice.util.WeakQueue;
import io.vlingo.xoom.wire.message.RawMessage;
import io.vlingo.xoom.wire.node.Id;

public final class GridApplicationMessageHandler implements ApplicationMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(GridApplicationMessageHandler.class);

  private final Id localNode;
  private final HashRing<Id> hashRing;
  private final GridActorControl.Inbound inbound;
  private final GridActorControl.Outbound outbound;
  private final Decoder decoder;
  private final Visitor visitor;
  private final Scheduler scheduler;

  private final HardRefHolder holder;

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
      final Runnable runnable = () -> {
        logger.debug("Handling message {} from {}", message, sender);
        message.accept(localNode, sender, visitor);
      };

      if (Objects.nonNull(holder)) {
        holder.holdOnTo(runnable);
      }

      // incoming messages are dispatched immediately
      runnable.run();
    } catch (Exception e) {
      logger.error(String.format("Failed to process message %s", raw), e);
    }
  }

  @Override
  public void disburse(final Id id) {
    // there are no buffered messages to be disbursed since incoming messages are dispatched immediately
  }


  final class ControlMessageVisitor implements Visitor {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void visit(final Id receiver, final Id sender, final Answer answer) {
      inbound.answer(receiver, sender, answer);
    }

    @Override
    public <T> void visit(final Id receiver, final Id sender, final Deliver<T> deliver) {
      final Id recipient = receiver(receiver, deliver.address);
      if (recipient == receiver) {
        inbound.deliver(
            receiver, sender,
            returnsAnswer(receiver, sender, deliver),
            deliver.protocol, deliver.address, deliver.definition, deliver.consumer, deliver.representation);
      } else {
        outbound.forward(recipient, sender, deliver);
      }
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
                    returnsAnswer(receiver, sender, deliver), deliver.representation))
            .collect(Collectors.toCollection(ArrayList::new));
        inbound.relocate(receiver, sender, relocate.definition,
            relocate.address, relocate.snapshot, pending);
      } else {
        outbound.forward(recipient, sender, relocate);
      }
    }

    private Returns<?> returnsAnswer(final Id receiver, final Id sender, final Deliver<?> deliver) {
      final Returns<?> returns;
      if (deliver.answerCorrelationId == null) {
        returns = null;
      } else {
        final Completes<Object> completes = Completes.using(scheduler);
        completes.andThen(result -> new Answer<>(deliver.answerCorrelationId, result))
                .recoverFrom(error -> new Answer<>(deliver.answerCorrelationId, error))
                .otherwise(ignored -> new Answer<>(deliver.answerCorrelationId, new TimeoutException()))
                .andThenConsume(4000,
                        answer -> outbound.answer(sender, receiver, answer))
                .andFinally();

        // 'root' Completes here! Completes#with(Object) will trigger the execution of whole Completes chain/pipeline built above
        returns = Returns.value(completes);
      }
      return returns;
    }
  }

}
