package io.vlingo.lattice.grid.application.message;

public interface Visitor {
  void visit(Answer answer);
  void visit(Deliver deliver);
  void visit(Start start);
}
