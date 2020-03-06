// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.GridActorControl;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.wire.node.Id;

public class InboundGridActorControl implements GridActorControl.Inbound {

  private final Logger logger;
  private final Grid grid;

  private final Function<UUID, Returns<?>> correlation;


  public InboundGridActorControl(Logger logger, Grid grid, Function<UUID, Returns<?>> correlation) {
    this.logger = logger;
    this.grid = grid;
    this.correlation = correlation;
  }


  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> void answer(Id receiver, Id sender, Answer<T> answer) {
    logger.debug("GRID: Processing application message: Answer");
    final Returns<Object> clientReturns = (Returns<Object>) correlation.apply(answer.correlationId);
    if (clientReturns == null) {
      logger.warn("GRID: Answer from {} for Returns with {} didn't match a Returns on this node!", sender, answer.correlationId);
      return;
    }
    if (answer.error == null) {
      T result = ActorProxyBase.thunk(grid, answer.result);
      if (clientReturns.isCompletes()) {
        clientReturns.asCompletes().with(result);
      } else if (clientReturns.isCompletableFuture()) {
        clientReturns.asCompletableFuture().complete(result);
      } else if (clientReturns.isFuture()) {
        ((CompletableFuture) clientReturns.asFuture()).complete(result);
      }
    }
    else {
      if (clientReturns.isCompletes()) {
        clientReturns.asCompletes().failed(new RuntimeException("Remote actor call failed", answer.error));
      } else if (clientReturns.isCompletableFuture()) {
        clientReturns.asCompletableFuture().completeExceptionally(answer.error);
      } else if (clientReturns.isFuture()) {
        ((CompletableFuture) clientReturns.asFuture()).completeExceptionally(answer.error);
      }
    }
  }

  @Override
  public void forward(Id receiver, Id sender, Message message) {
    throw new UnsupportedOperationException("Should have been handled in Visitor#accept(Id, Id, Forward) by dispatching the visitor to the enclosed Message");
  }

  @Override
  public <T> void start(Id receiver, Id sender, Class<T> protocol, Address address, Definition.SerializationProxy definition) {
    logger.debug("Processing: Received application message: Start");
    final GridActor<?> actor = (GridActor<?>) grid.actorAt(address);
    if (actor == null) {
      grid.actorFor(protocol, Definition.from(grid, definition, grid.world.defaultLogger()), address);
    }
    else if (actor.isSuspended()) {
      logger.debug("Resuming thunk found at {} with definition='{}'", address, actor.definition());
      actor.resume();
    }
  }

  @Override
  public <T> void deliver(
      Id receiver, Id sender, Returns<?> returns, Class<T> protocol, Address address, Definition.SerializationProxy definition, SerializableConsumer<T> consumer, String representation) {
    logger.debug("Processing: Received application message: Deliver");
    Optional<Actor> maybeActor = Optional.ofNullable(grid.actorAt(address));
    final Actor actor = maybeActor.orElseGet(() -> {
      grid.actorThunkFor(protocol, Definition.from(grid, definition, grid.world.defaultLogger()), address);
      return grid.actorAt(address);
    });
    Mailbox mailbox = actor.lifeCycle.environment.mailbox;
    mailbox.send(actor, protocol, consumer, returns, representation);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void relocate(Id receiver, Id sender, Definition.SerializationProxy definition, Address address, Object snapshot, List<? extends io.vlingo.actors.Message> pending) {
    logger.debug("Processing: Received application message: Relocate");
    final Optional<GridActor<?>> maybeActor = Optional.ofNullable((GridActor<?>) grid.actorAt(address));
    final RelocationSnapshotConsumer<Object> consumer = maybeActor.map(a -> grid.actorAs(a, RelocationSnapshotConsumer.class))
        .orElseGet(() -> grid.actorFor(
            RelocationSnapshotConsumer.class, Definition.from(grid, definition, grid.world.defaultLogger()), address));
    consumer.applyRelocationSnapshot(snapshot);
    final GridActor<?> actor = maybeActor.orElseGet(() -> (GridActor<?>) grid.actorAt(address));
    pending.forEach(m -> {
      final LocalMessage<?> message = (LocalMessage<?>)m;
      message.set(actor,
          message.protocol(), message.consumer(),
          message.returns(), message.representation());
      message.deliver();
    });
    actor.resume();
  }
}
