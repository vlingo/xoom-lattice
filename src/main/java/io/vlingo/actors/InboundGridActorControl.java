package io.vlingo.actors;

import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.GridActorControl;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.wire.node.Id;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
        clientReturns.asCompletes().failed(); // TODO add support for failing with a specific error
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
  @SuppressWarnings("unchecked")
  public <T> void start(Id receiver, Id sender, Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters) {
    logger.debug("Processing: Received application message: Start");
    final GridActor<?> actor = (GridActor<?>) grid.actorAt(address);
    if (actor == null) {
      grid.actorFor(protocol, Definition.has(
          type,
          parameters == null
              ? Collections.EMPTY_LIST
              : Arrays.asList(parameters)),
          address);
    }
    else if (actor.isSuspended()) {
      // a thunk was created due to receiving Answer or Deliver
      // perhaps require a constructor from snapshot?
      actor.resume();
    }
  }

  @Override
  public <T> void deliver(
      Id receiver, Id sender, Returns<?> returns, Class<T> protocol, Address address, Class<? extends Actor> type, SerializableConsumer<T> consumer, String representation) {
    logger.debug("Processing: Received application message: Deliver");
    Optional<Actor> maybeActor = Optional.ofNullable(grid.actorAt(address));
    final Actor actor = maybeActor.orElseGet(() -> {
      grid.actorThunkFor(protocol, type, address);
      return grid.actorAt(address);
    });
    Mailbox mailbox = actor.lifeCycle.environment.mailbox;
    mailbox.send(actor, protocol, consumer, returns, representation);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void relocate(Id receiver, Id sender, Class<? extends Actor> type, Address address, Serializable snapshot, List<? extends io.vlingo.actors.Message> pending) {
    logger.debug("Processing: Received application message: Relocate");
    final Optional<GridActor<?>> maybeActor = Optional.ofNullable((GridActor<?>) grid.actorAt(address));
    final RelocationSnapshotConsumer<Serializable> consumer = maybeActor.map(a -> grid.actorAs(a, RelocationSnapshotConsumer.class))
        .orElseGet(() -> grid.actorFor(RelocationSnapshotConsumer.class, Definition.has(type, Collections.emptyList()), address));
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
