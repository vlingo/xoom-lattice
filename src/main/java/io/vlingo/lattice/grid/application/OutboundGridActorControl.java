package io.vlingo.lattice.grid.application;

import io.vlingo.cluster.model.CommunicationsHub;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Deliver;
import io.vlingo.lattice.grid.application.message.Start;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

public class OutboundGridActorControl implements GridActorControl.Outbound {

  private final CommunicationsHub hub;

  public OutboundGridActorControl(CommunicationsHub hub) {
    this.hub = hub;
  }

  @Override
  public void start(Start start) {
    RawMessage message = RawMessage.from(start.sender, -1, "Hello Start()");
    hub.applicationOutboundStream().sendTo(message, Id.of(start.recipient));
  }

  @Override
  public void deliver(Deliver deliver) {
    RawMessage message = RawMessage.from(deliver.sender, -1, "Hello Start()");
    hub.applicationOutboundStream().sendTo(message, Id.of(deliver.recipient));
  }

  @Override
  public void answer(Answer answer) {
    RawMessage message = RawMessage.from(answer.sender, -1, "Hello Start()");
    hub.applicationOutboundStream().sendTo(message, Id.of(answer.recipient));
  }
}
