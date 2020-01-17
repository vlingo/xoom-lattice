package io.vlingo.lattice.grid.application.message;

import java.io.Serializable;

public class Answer implements Serializable, Message {
  private static final long serialVersionUID = 1L;

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
