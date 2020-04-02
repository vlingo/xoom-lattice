package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.wire.node.Id;

public class Standby<T> implements Message {

  public final Class<T> protocol;
  public final Address address;
  public final Definition.SerializationProxy definition;

  public Standby(Class<T> protocol, Address address, Definition.SerializationProxy definition) {
    this.protocol = protocol;
    this.address = address;
    this.definition = definition;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }
}
