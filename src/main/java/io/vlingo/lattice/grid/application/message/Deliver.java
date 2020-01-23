package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Address;

import java.io.Serializable;

public class Deliver<T> implements Serializable, Message {
  private static final long serialVersionUID = 591702431591762704L;

  public final Class<T> protocol;
  public final Address address;
  public final String representation;

  public Deliver(final Class<T> protocol, final Address address, final String representation) {
    this.protocol = protocol;
    this.address = address;
    this.representation = representation;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return String.format(
        "Deliver(protocol='%s', address='%s', representation='%s')",
        protocol, address, representation);
  }
}
