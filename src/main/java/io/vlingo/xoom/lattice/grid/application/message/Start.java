// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application.message;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.wire.node.Id;

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
