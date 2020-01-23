package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;

import java.io.Serializable;

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
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
