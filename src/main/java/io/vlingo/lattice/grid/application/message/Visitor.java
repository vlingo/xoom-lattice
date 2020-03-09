package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.node.Id;

public interface Visitor {
  <T> void visit(Id receiver, Id sender, Answer<T> answer);
  <T> void visit(Id receiver, Id sender, Deliver<T> deliver);
  <T> void visit(Id receiver, Id sender, Start<T> start);
  void visit(Id receiver, Id sender, Relocate relocate);
  void visit(Id receiver, Id sender, Recover recover);
  default void visit(Id receiver, Id sender, Forward forward) {
    forward.message.accept(receiver, forward.originalSender, this);
  }
}
