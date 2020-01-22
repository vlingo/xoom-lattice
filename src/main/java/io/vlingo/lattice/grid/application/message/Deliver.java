package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.node.Id;

import java.io.Serializable;

public class Deliver extends Message.Impl implements Serializable {

  private static final long serialVersionUID = 591702431591762704L;

  public Deliver(short sender, short recipient) {
    super(sender, recipient);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
