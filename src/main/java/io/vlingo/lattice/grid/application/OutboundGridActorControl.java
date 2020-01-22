package io.vlingo.lattice.grid.application;

import io.vlingo.cluster.model.CommunicationsHub;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Deliver;
import io.vlingo.lattice.grid.application.message.Start;
import io.vlingo.wire.node.Id;

public class OutboundGridActorControl implements GridActorControl.Outbound {

  private final CommunicationsHub hub;

  public OutboundGridActorControl(CommunicationsHub hub) {
    this.hub = hub;
  }

  @Override
  public void start(Start start) {
    hub.applicationOutboundStream().sendTo(null, Id.of(start.recipient));
  }

  @Override
  public void deliver(Deliver deliver) {

  }

  @Override
  public void answer(Answer answer) {

  }
}
