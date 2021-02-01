
// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.wire.node.Id;

import java.util.List;

public class Relocate implements Message {
  private static final long serialVersionUID = 8992847890818617297L;

  public final Address address;
  public final Definition.SerializationProxy definition;
  public final Object snapshot;
  public final List<Deliver<?>> pending;

  public Relocate(Address address, Definition.SerializationProxy definition, Object snapshot, List<Deliver<?>> pending) {
    this.address = address;
    this.definition = definition;
    this.snapshot = snapshot;
    this.pending = pending;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format("Relocate(address='%s', definitionProxy='%s', snapshot='%s', pending='%s')",
        address, definition, snapshot, pending);
  }
}
