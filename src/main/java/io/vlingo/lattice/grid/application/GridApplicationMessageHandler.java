// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vlingo.actors.Address;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Returns;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Decoder;
import io.vlingo.lattice.grid.application.message.Deliver;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.lattice.grid.application.message.Relocate;
import io.vlingo.lattice.grid.application.message.Start;
import io.vlingo.lattice.grid.application.message.Visitor;
import io.vlingo.lattice.grid.application.message.serialization.JavaObjectDecoder;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.lattice.util.HardRefHolder;
import io.vlingo.lattice.util.WeakQueue;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

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
  private final Queue<Runnable> buffer = new WeakQueue<>();

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
  public void handle(RawMessage raw) {
    try {
      Message message = decoder.decode(raw.asBinaryMessage());
      Id sender = Id.of(raw.header().nodeId());
      logger.debug("Buffering message {} from {}", message, sender);
      final Runnable runnable = () -> {
        logger.debug("Handling message {} from {}", message, sender);
        message.accept(localNode, sender, visitor);
      };
      buffer.offer(runnable);
      if (Objects.nonNull(holder)) {
        holder.holdOnTo(runnable);
      }
    } catch (Exception e) {
      logger.error(String.format("Failed to process message %s", raw), e);
    }
  }

  @Override
  public void disburse(Id id) {
    if (!id.equals(localNode)) return;
    logger.debug("Disbursing buffered messages");
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
    public void visit(Id receiver, Id sender, Answer answer) {
      inbound.answer(receiver, sender, answer);
    }

    @Override
    public <T> void visit(Id receiver, Id sender, Deliver<T> deliver) {
      Id recipient = receiver(receiver, deliver.address);
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
    public <T> void visit(Id receiver, Id sender, Start<T> start) {
      Id recipient = receiver(receiver, start.address);
      if (recipient == receiver) {
        inbound.start(receiver, sender, start.protocol, start.address, start.definition);
      } else {
        outbound.forward(recipient, sender, start);
      }
    }

    private Id receiver(Id receiver, Address address) {
      final Id recipient = hashRing.nodeOf(address.idString());
      if (recipient == null || recipient.equals(receiver)) {
        return receiver;
      }
      return recipient;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void visit(Id receiver, Id sender, Relocate relocate) {
      Id recipient = receiver(receiver, relocate.address);
      if (recipient == receiver) {
        List<LocalMessage> pending = relocate.pending.stream()
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

    private Returns<?> returnsAnswer(Id receiver, Id sender, Deliver<?> deliver) {
      final Returns<?> returns;
      if (deliver.answerCorrelationId == null) {
        returns = null;
      } else {
        returns = Returns.value(
            Completes.using(scheduler)
                .andThen(result -> new Answer<>(deliver.answerCorrelationId, result))
                .recoverFrom(error -> new Answer<>(deliver.answerCorrelationId, error))
                .otherwise(ignored -> new Answer<>(deliver.answerCorrelationId, new TimeoutException()))
                .andThenConsume(4000,
                    answer -> outbound.answer(sender, receiver, answer))
                .andFinally()
        );
      }
      return returns;
    }
  }

}
