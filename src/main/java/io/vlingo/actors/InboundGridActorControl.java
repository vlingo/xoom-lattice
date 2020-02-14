package io.vlingo.actors;

import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.GridActorControl;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.wire.node.Id;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
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
  public <T> void start(Id receiver, Id sender, Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters) {
    logger.debug("Processing: Received application message: Start");
    grid.actorFor(protocol, Definition.has(
        type,
        parameters == null
            ? Collections.EMPTY_LIST
            : Arrays.asList(parameters)),
        address);
  }

  @Override
  public <T> void deliver(
      Id receiver, Id sender, Returns<?> returns, Class<T> protocol, Address address, SerializableConsumer<T> consumer, String representation) {
    logger.debug("Processing: Received application message: Deliver");
    Actor actor = grid.actorAt(address);
    // TODO handle null by
    Mailbox mailbox = actor.lifeCycle.environment.mailbox;
    mailbox.send(actor, protocol, consumer, returns, representation);
  }
}
