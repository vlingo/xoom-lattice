package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.wire.node.Id;

import java.io.Serializable;

public class Start<T> implements Serializable, Message {
  private static final long serialVersionUID = -7081324662923459283L;

  public final Class<T> protocol;
  public final Address address;
  public final Definition.SerializationProxy definition;

  public Start(Class<T> protocol, Address address, Definition.SerializationProxy definition) {
    this.protocol = protocol;
    this.address = address;
    this.definition = definition;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format(
        "Start(protocol='%s', address='%s', definition='%s')",
        protocol, address, definition);
  }
}
