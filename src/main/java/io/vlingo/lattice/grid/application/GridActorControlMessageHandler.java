package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Address;
import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.lattice.grid.application.message.serialization.JavaObjectDecoder;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

public final class GridActorControlMessageHandler implements ApplicationMessageHandler {

  private final Id localNode;
  private final HashRing<Id> hashRing;
  private final GridActorControl.Inbound inbound;
  private final GridActorControl.Outbound outbound;
  private final Decoder decoder;
  private final Visitor visitor;

  public GridActorControlMessageHandler(
      final Id localNode, final HashRing<Id> hashRing,
      final GridActorControl.Inbound inbound,
      final GridActorControl.Outbound outbound) {
    this(localNode, hashRing, inbound, outbound, new JavaObjectDecoder());
  }

  public GridActorControlMessageHandler(
      final Id localNode, final HashRing<Id> hashRing,
      final GridActorControl.Inbound inbound,
      final GridActorControl.Outbound outbound,
      final Decoder decoder) {
    this.localNode = localNode;
    this.hashRing = hashRing;
    this.inbound = inbound;
    this.outbound = outbound;
    this.decoder = decoder;

    this.visitor = new ControlMessageVisitor();
  }

  public void handle(RawMessage raw) {
    try {
      Message message = decoder.decode(raw.asBinaryMessage());
      System.out.println(message);
      Id sender = Id.of(raw.header().nodeId());
      message.accept(localNode, sender, visitor);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  final class ControlMessageVisitor implements Visitor {
    @Override
    public void visit(Id receiver, Id sender, Answer answer) {
      inbound.answer(null, null, answer);
    }

    @Override
    public <T> void visit(Id receiver, Id sender, Deliver<T> deliver) {
      Id recipient = receiver(receiver, deliver.address);
      if (recipient == receiver) {
        inbound.deliver(receiver, sender, deliver.protocol, deliver.address, deliver.consumer, deliver.representation);
      }
      else {
        outbound.forward(recipient, sender, deliver);
      }
    }

    @Override
    public <T> void visit(Id receiver, Id sender, Start<T> start) {
      Id recipient = receiver(receiver, start.address);
      if (recipient == receiver) {
        inbound.start(receiver, sender, start.protocol, start.address, start.type, start.parameters);
      }
      else {
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
  }

}
