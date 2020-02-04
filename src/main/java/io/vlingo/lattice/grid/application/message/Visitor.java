package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.node.Id;

public interface Visitor {
  void visit(Id recipient, Id sender, Answer answer);
  <T> void visit(Id recipient, Id sender, Deliver<T> deliver);
  <T> void visit(Id recipient, Id sender, Start<T> start);
  default void visit(Id recipient, Id sender, Forward forward) {
    forward.message.accept(recipient, forward.originalSender, this);
  }
}
