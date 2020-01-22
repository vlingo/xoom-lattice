package io.vlingo.lattice.grid.application.message;

import java.io.Serializable;

public interface Message extends Serializable {
  void accept(Visitor visitor);

  abstract class Impl implements Message {

    private static final long serialVersionUID = -5907116681960030343L;

    public final short sender;
    public final short recipient;

    protected Impl(short sender, short recipient) {
      this.sender = sender;
      this.recipient = recipient;
    }
  }
}
