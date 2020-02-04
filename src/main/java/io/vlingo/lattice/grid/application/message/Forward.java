package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.node.Id;

public class Forward implements Message {
  private static final long serialVersionUID = 2414354829740671726L;

  public final Id originalSender;
  public final Message message;

  public Forward(Id originalSender, Message message) {
    this.originalSender = originalSender;
    this.message = message;
  }

  @Override
  public void accept(Id recipient, Id sender, Visitor visitor) {
    visitor.visit(recipient, sender, this);
  }

  @Override
  public String toString() {
    return String.format("Forward(originalSender='%s', message='%s')",
        originalSender, message);
  }
}
