package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.node.Id;

public interface Visitor {
  void visit(Id receiver, Id sender, Answer answer);
  <T> void visit(Id receiver, Id sender, Deliver<T> deliver);
  <T> void visit(Id receiver, Id sender, Start<T> start);
  default void visit(Id receiver, Id sender, Forward forward) {
    forward.message.accept(receiver, forward.originalSender, this);
  }
}
