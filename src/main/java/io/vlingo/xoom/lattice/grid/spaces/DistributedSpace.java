// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.grid.Grid;

public interface DistributedSpace extends Space {
  <T> Completes<KeyItem<T>> localPut(final Key key, final Item<T> item);
  <T> Completes<KeyItem<T>> localTake(final Key key, final Period until);

  class DistributedSpaceInstantiator implements ActorInstantiator<DistributedSpaceActor> {
    private final String accessorName;
    private final String spaceName;
    private final Space localSpace;
    private final Grid grid;

    public DistributedSpaceInstantiator(String accessorName, String spaceName, Space localSpace, Grid grid) {
      this.accessorName = accessorName;
      this.spaceName = spaceName;
      this.localSpace = localSpace;
      this.grid = grid;
    }

    @Override
    public DistributedSpaceActor instantiate() {
      return new DistributedSpaceActor(accessorName, spaceName, localSpace, grid);
    }

    @Override
    public Class<DistributedSpaceActor> type() {
      return DistributedSpaceActor.class;
    }
  }
}