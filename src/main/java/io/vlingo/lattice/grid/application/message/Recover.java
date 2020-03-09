package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.wire.node.Id;

public class Recover implements Message {

  public final Address address;
  public final Definition.SerializationProxy definition;

  public Recover(Address address, Definition.SerializationProxy definition) {
    this.address = address;
    this.definition = definition;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }
}
