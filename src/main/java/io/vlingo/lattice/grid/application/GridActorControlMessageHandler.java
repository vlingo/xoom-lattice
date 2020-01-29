package io.vlingo.lattice.grid.application;

import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public final class GridActorControlMessageHandler implements ApplicationMessageHandler {

  private final GridActorControl.Inbound control;
  private final Visitor visitor;
  private final Decoder decoder;

  public GridActorControlMessageHandler(final GridActorControl.Inbound control) {
    this.control = control;
    this.visitor = new ControlMessageVisitor();
    this.decoder = new JavaObjectMessageDecoder();
  }

  public void handle(RawMessage raw) {
    try {
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
      control.answer(null, null, answer);
    }

    @Override
    public <T> void visit(Id recipient, Id sender, Deliver<T> deliver) {
      control.deliver(recipient, sender, deliver.protocol, deliver.address, deliver.consumer, deliver.representation);
    }

    @Override
    public <T> void visit(Id recipient, Id sender, Start<T> start) {
      control.start(recipient, sender, start.protocol, start.address, start.type, start.parameters);
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
