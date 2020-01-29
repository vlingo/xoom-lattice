package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.node.Id;

import java.io.Serializable;

public interface Message extends Serializable {
  void accept(Id recipient, Id sender, Visitor visitor);
}
