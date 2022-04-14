
// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.lattice.grid.application.message;

import java.util.List;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.wire.node.Id;

public class Relocate implements Message {
  private static final long serialVersionUID = 8992847890818617297L;

  public final Address address;
  public final Definition.SerializationProxy definition;
  public final Object snapshot;
  public final List<GridDeliver<?>> pending;

  public Relocate(Address address, Definition.SerializationProxy definition, Object snapshot, List<GridDeliver<?>> pending) {
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
