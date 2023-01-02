// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application.message;

import io.vlingo.xoom.wire.node.Id;

public class Forward implements Message {
  private static final long serialVersionUID = 2414354829740671726L;

  public final Id originalSender;
  public final Message message;

  public Forward(Id originalSender, Message message) {
    this.originalSender = originalSender;
    this.message = message;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format("Forward(originalSender='%s', message='%s')",
        originalSender, message);
  }
}
