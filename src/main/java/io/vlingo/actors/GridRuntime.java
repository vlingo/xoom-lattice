// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.lattice.grid.GridNodeBootstrap;
import io.vlingo.lattice.grid.application.OutboundGridActorControl;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.wire.node.Id;

public interface GridRuntime {
  Actor actorAt(Address address);
  GridNodeBootstrap gridNodeBootstrap();
  HashRing<Id> hashRing();
  void nodeJoined(final Id newNode);
  void nodeLeft(final Id removedNode);
  void setNodeId(final Id nodeId);
  void setOutbound(final OutboundGridActorControl outbound);
}
