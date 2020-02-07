package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.wire.node.Id;

import java.util.List;

public class Relocate<T> {//implements Message {
  private static final long serialVersionUID = 8992847890818617297L;

  public final Class<T> protocol;
  public final Address address;
  public final Class<? extends Actor> type;
  public final Object[] parameters;

  public final Actor state;

  public final List<Deliver<T>> pending;

  public Relocate(Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters, Actor state, List<Deliver<T>> pending) {
    this.protocol = protocol;
    this.address = address;
    this.type = type;
    this.parameters = parameters;
    this.state = state;
    this.pending = pending;
  }

//  @Override
//  public void accept(Id receiver, Id sender, Visitor visitor) {
//    visitor.visit(receiver, sender, this);
//  }
}
