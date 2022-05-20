// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.lattice.grid.application.GridActorControl;
import io.vlingo.xoom.lattice.grid.application.QuorumObserver;
import io.vlingo.xoom.lattice.grid.hashring.HashRing;
import io.vlingo.xoom.wire.node.Id;
import io.vlingo.xoom.wire.node.Node;

import java.util.Collection;

public interface GridRuntime extends QuorumObserver {
  Actor actorAt(Address address);
  void relocateActors();
  Stage asStage();
  Stage localStage();
  GridNodeBootstrap gridNodeBootstrap();
  HashRing<Id> hashRing();
  void nodeJoined(final Id newNode);
  void informAllLiveNodes(final Collection<Node> liveNodes);
  QuorumObserver quorumObserver();
  void setNodeId(final Id nodeId);
  void setOutbound(final GridActorControl.Outbound outbound);
  World world();
  ClassLoader worldClassLoader();
}
