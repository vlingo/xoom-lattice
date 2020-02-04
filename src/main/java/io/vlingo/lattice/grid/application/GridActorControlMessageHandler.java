package io.vlingo.lattice.grid.application;

import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public final class GridActorControlMessageHandler implements ApplicationMessageHandler {

  private final GridActorControl.Inbound inbound;
  private final GridActorControl.Outbound outbound;
  private final Decoder decoder;
  private final Visitor visitor;

  public GridActorControlMessageHandler(final GridActorControl.Inbound inbound, final GridActorControl.Outbound outbound) {
    this(inbound, outbound, new JavaObjectMessageDecoder());
  }

  public GridActorControlMessageHandler(final GridActorControl.Inbound inbound, final GridActorControl.Outbound outbound, final Decoder decoder) {
    this.inbound = inbound;
    this.outbound = outbound;
    this.decoder = decoder;
    this.visitor = new ControlMessageVisitor();
  }

  public void handle(RawMessage raw) {
    try {
      // TODO implement outbound forward
      Message message = decoder.decode(raw.asBinaryMessage());
      System.out.println(message);
      Id recipient = null; // TODO should be this node
      Id sender = Id.of(raw.header().nodeId());
      message.accept(recipient, sender, visitor);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  final class ControlMessageVisitor implements Visitor {
    @Override
    public void visit(Id recipient, Id sender, Answer answer) {
      inbound.answer(null, null, answer);
    }

    @Override
    public <T> void visit(Id recipient, Id sender, Deliver<T> deliver) {
      inbound.deliver(recipient, sender, deliver.protocol, deliver.address, deliver.consumer, deliver.representation);
    }

    @Override
    public <T> void visit(Id recipient, Id sender, Start<T> start) {
      inbound.start(recipient, sender, start.protocol, start.address, start.type, start.parameters);
    }
  }

  static final class JavaObjectMessageDecoder implements Decoder {

    @Override
    public Message decode(byte[] bytes) {
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      try (ObjectInputStream in = new ObjectInputStream(bis)) {
        return (Message) in.readObject();
      } catch (IOException | ClassNotFoundException e) {
        throw new IllegalArgumentException("decode failed", e);
      }
    }
  }
}
