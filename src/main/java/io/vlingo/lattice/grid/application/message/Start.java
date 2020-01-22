package io.vlingo.lattice.grid.application.message;

import java.io.Serializable;

public class Start extends Message.Impl implements Serializable {

  private static final long serialVersionUID = -7081324662923459283L;

  public Start(short sender, short recipient) {
    super(sender, recipient);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
