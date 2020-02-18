package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.wire.node.Id;

import java.io.Serializable;
import java.util.List;

public class Relocate implements Message {
  private static final long serialVersionUID = 8992847890818617297L;

  public final Address address;
  public final Class<? extends Actor> type;
  public final Serializable snapshot;
  public final List<Deliver<?>> pending;

  public Relocate(Class<? extends Actor> type, Address address, Serializable snapshot, List<Deliver<?>> pending) {
    this.address = address;
    this.type = type;
    this.snapshot = snapshot;
    this.pending = pending;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format("Relocate(address='%s', type='%s', snapshot='%s', pending='%s')",
        address, type, snapshot, pending);
  }
}
