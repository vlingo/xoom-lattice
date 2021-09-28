// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.actors.ActorProxyBase;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.__InternalOnlyAccessor;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.application.GridActorControl;
import io.vlingo.xoom.lattice.grid.application.message.Answer;
import io.vlingo.xoom.lattice.grid.application.message.UnAckMessage;
import io.vlingo.xoom.wire.node.Id;

public class InboundGridActorControl extends Actor implements GridActorControl.Inbound {

  private final GridRuntime gridRuntime;

  private final Function<UUID, UnAckMessage> correlation;
  private final Function<UUID, Returns<?>> correlation2;

  public InboundGridActorControl(final GridRuntime gridRuntime, final Function<UUID, UnAckMessage> correlation, final Function<UUID, Returns<?>> correlation2) {
    this.gridRuntime = gridRuntime;
    this.correlation = correlation;
    this.correlation2 = correlation2;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> void answer(final Id receiver, final Id sender, final Answer<T> answer) {
    // same Answer is used for both Deliver and Deliver2 message
    logger().debug("GRID: Processing application message: Answer");
    Returns<Object> clientReturns = correlation.apply(answer.correlationId).getReturns();
    if (clientReturns == null) {
      clientReturns = (Returns<Object>) correlation2.apply(answer.correlationId);
      if (clientReturns == null) {
        logger().warn("GRID: Answer from {} for Returns with {} didn't match a Returns on this node!", sender, answer.correlationId);
        return;
      }
    }
    if (answer.error == null) {
      T result = ActorProxyBase.thunk(gridRuntime.asStage(), answer.result);
      if (clientReturns.isCompletes()) {
        clientReturns.asCompletes().with(result);
      } else if (clientReturns.isCompletableFuture()) {
        clientReturns.asCompletableFuture().complete(result);
      } else if (clientReturns.isFuture()) {
        ((CompletableFuture) clientReturns.asFuture()).complete(result);
      }
    } else {
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
  public void forward(final Id receiver, final Id sender, final io.vlingo.xoom.lattice.grid.application.message.Message message) {
    throw new UnsupportedOperationException("Should have been handled in Visitor#accept(Id, Id, Forward) by dispatching the visitor to the enclosed Message");
  }

  @Override
  public <T> void start(
          final Id receiver,
          final Id sender,
          final Class<T> protocol,
          final Address address,
          final Definition.SerializationProxy definition) {

    logger().debug("Processing: Received application message: Start");

    final Stage stage = gridRuntime.asStage();

    final Actor actor =
            __InternalOnlyAccessor.rawLookupOrStart(
                    stage,
                    Definition.from(stage, definition, stage.world().defaultLogger()),
                    address);

    if (GridActorOperations.isSuspendedForRelocation(actor)) {
      logger().debug("Resuming thunk found at {} with definition='{}'",
          address,
          actor.definition());

      GridActorOperations.resumeFromRelocation(actor);
    }
  }

  @Override
  public <T> void deliver(
          final Id receiver,
          final Id sender,
          final Returns<?> returns,
          final Class<T> protocol,
          final Address address,
          final Definition.SerializationProxy definition,
          final SerializableConsumer<T> consumer,
          final String representation) {

    logger().debug("Processing: Received application message: Deliver");

    final Stage stage = gridRuntime.asStage();

    final Actor actor =
            __InternalOnlyAccessor.actorLookupOrStartThunk(
                    stage,
                    Definition.from(stage, definition, stage.world().defaultLogger()),
                    address);

    __InternalOnlyAccessor.actorMailbox(actor).send(actor, protocol, consumer, returns, representation);

    if (GridActorOperations.isSuspendedForRelocation(actor)) {
      // this case is happening when a message is retried on a different node and above actor is created 'on demand'
      logger().debug("Resuming thunk found at {} with definition='{}'",
              address,
              actor.definition());

      GridActorOperations.resumeFromRelocation(actor);
    }
  }

  @Override
  public <T> void deliver(
          Id recipient,
          Id sender,
          Returns<?> returns,
          Class<T> protocol, Function<Grid, Actor> actorProvider,
          SerializableConsumer<T> consumer,
          String representation) {
    logger().debug("Processing: Received application message: Deliver2");

    final Grid grid = (Grid) gridRuntime.asStage();

    final Actor actor = actorProvider.apply(grid);

    __InternalOnlyAccessor.actorMailbox(actor).send(actor, protocol, consumer, returns, representation);
  }

  @Override
  public void relocate(
          final Id receiver,
          final Id sender,
          final Definition.SerializationProxy definition,
          final Address address,
          final Object snapshot,
          final List<? extends io.vlingo.xoom.actors.Message> pending) {

    logger().debug("Processing: Received application message: Relocate");

    final Stage stage = gridRuntime.asStage();

    final Actor actor =
            __InternalOnlyAccessor.actorLookupOrStartThunk(
                    stage,
                    Definition.from(stage, definition, stage.world().defaultLogger()),
                    address);

    GridActorOperations.applyRelocationSnapshot(stage, actor, snapshot);

    final Mailbox mailbox = __InternalOnlyAccessor.actorMailbox(actor);

    pending.forEach(pendingMessage -> {
      final LocalMessage<?> message = (LocalMessage<?>) pendingMessage;
      message.set(actor, message.protocol(), message.consumer(), message.returns(), message.representation());
      mailbox.send(message);
    });

    GridActorOperations.resumeFromRelocation(actor);
  }

  @Override
  public void informNodeIsHealthy(Id id, boolean isHealthy) {
    throw new UnsupportedOperationException("informNodeIsHealthy handled in ApplicationMessageHandler");
  }

  public static class InboundGridActorControlInstantiator implements ActorInstantiator<InboundGridActorControl> {
    private static final long serialVersionUID = 1494058617174306163L;

    private final GridRuntime gridRuntime;
    private final Function<UUID, UnAckMessage> correlation;
    private final Function<UUID, Returns<?>> correlation2;

    public InboundGridActorControlInstantiator(final GridRuntime gridRuntime, final Function<UUID, UnAckMessage> correlation, final Function<UUID, Returns<?>> correlation2) {
      this.gridRuntime = gridRuntime;
      this.correlation = correlation;
      this.correlation2 = correlation2;
    }

    @Override
    public InboundGridActorControl instantiate() {
      return new InboundGridActorControl(gridRuntime, correlation, correlation2);
    }
  }
}
