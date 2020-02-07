package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.wire.node.Id;

import java.io.Serializable;
import java.util.Arrays;

public class Start<T> implements Serializable, Message {
  private static final long serialVersionUID = -7081324662923459283L;

  public final Class<T> protocol;
  public final Address address;
  public final Class<? extends Actor> type;
  public final Object[] parameters;

  public Start(Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters) {
    this.protocol = protocol;
    this.address = address;
    this.type = type;
    this.parameters = parameters;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format(
        "Start(protocol='%s', address='%s', type='%s', parameters='%s')",
        protocol, address, type, Arrays.toString(parameters));
  }
}
