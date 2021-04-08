// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application.message;

import io.vlingo.xoom.wire.node.Id;

public interface Visitor {
  <T> void visit(Id receiver, Id sender, Answer<T> answer);
  <T> void visit(Id receiver, Id sender, Deliver<T> deliver);
  <T> void visit(Id receiver, Id sender, Start<T> start);
  void visit(Id receiver, Id sender, Relocate relocate);
  default void visit(Id receiver, Id sender, Forward forward) {
    forward.message.accept(receiver, forward.originalSender, this);
  }
}
