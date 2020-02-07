package io.vlingo.actors;

import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.GridActorControl;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.wire.node.Id;

import java.util.Arrays;
import java.util.Collections;

public class InboundGridActorControl implements GridActorControl.Inbound {

  private final Logger logger;
  private final Grid grid;

  public InboundGridActorControl(Logger logger, Grid grid) {
    this.logger = logger;
    this.grid = grid;
  }


  @Override
  public void answer(Id receiver, Id ref, Answer answer) {
    logger.debug("GRID: Received application message: Answer");
  }

  @Override
  public void forward(Id receiver, Id sender, Message message) {
    throw new UnsupportedOperationException("Should have been handled in Visitor#accept(Id, Id, Forward) by dispatching the visitor to the enclosed Message");
  }

  @Override
  public <T> void start(Id receiver, Id sender, Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters) {
    logger.debug("GRID: Received application message: Start");
    grid.actorFor(protocol, Definition.has(
        type,
        parameters == null
            ? Collections.EMPTY_LIST
            : Arrays.asList(parameters)),
        address);
  }

  @Override
  public <T> void deliver(Id receiver, Id ref, Class<T> protocol, Address address, SerializableConsumer<T> consumer, String representation) {
    logger.debug("GRID: Received application message: Deliver");
    Actor actor = grid.actorAt(address);
    Mailbox mailbox = actor.lifeCycle.environment.mailbox;
    mailbox.send(actor, protocol, consumer, null, representation);
  }
}
