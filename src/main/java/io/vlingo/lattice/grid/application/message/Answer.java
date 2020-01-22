package io.vlingo.lattice.grid.application.message;

import java.io.Serializable;

public class Answer extends Message.Impl implements Serializable {

  private static final long serialVersionUID = -2796142731077588067L;

  public Answer(short sender, short recipient) {
    super(sender, recipient);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
