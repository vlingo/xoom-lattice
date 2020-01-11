// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.time.Duration;
import java.util.Optional;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorInstantiator;
import io.vlingo.common.Completes;

public interface Space {
  <T> Completes<T> itemFor(final Class<T> protocol, final Class<? extends Actor> type, final Object...parameters);
  <T> Completes<KeyItem<T>> put(final Key key, final Item<T> item);
  <T> Completes<Optional<KeyItem<T>>> get(final Key key, final Period until);
  <T> Completes<Optional<KeyItem<T>>> take(final Key key, final Period until);

  static class SpaceInstantiator implements ActorInstantiator<SpaceActor> {
    private final Duration defaultScanInterval;

    public SpaceInstantiator(final Duration defaultScanInterval) {
      this.defaultScanInterval = defaultScanInterval;
    }

    @Override
    public SpaceActor instantiate() {
      return new SpaceActor(defaultScanInterval);
    }

    @Override
    public Class<SpaceActor> type() {
      return SpaceActor.class;
    }
  }

  static class PartitioningSpaceRouterInstantiator implements ActorInstantiator<PartitioningSpaceRouter> {
    private final int totalPartitions;
    private final Duration defaultScanInterval;

    public PartitioningSpaceRouterInstantiator(final int totalPartitions, final Duration defaultScanInterval) {
      this.totalPartitions = totalPartitions;
      this.defaultScanInterval = defaultScanInterval;
    }

    @Override
    public PartitioningSpaceRouter instantiate() {
      return new PartitioningSpaceRouter(totalPartitions, defaultScanInterval);
    }

    @Override
    public Class<PartitioningSpaceRouter> type() {
      return PartitioningSpaceRouter.class;
    }
  }
}
