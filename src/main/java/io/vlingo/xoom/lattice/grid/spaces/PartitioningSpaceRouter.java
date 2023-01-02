// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import java.time.Duration;
import java.util.Optional;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.common.Completes;

public class PartitioningSpaceRouter extends Actor implements Space {
  private Space[] partitions;
  private final int totalPartitions;

  public PartitioningSpaceRouter(final int totalPartitions, final Duration defaultScanInterval) {
    this.totalPartitions = totalPartitions;
    this.partitions = new Space[totalPartitions];

    initialize(defaultScanInterval);
  }

  @Override
  public <T> Completes<T> itemFor(final Class<T> protocol, final Class<? extends Actor> type, final Object... parameters) {
    // Fail; not implemented. See SpaceItemFactoryRelay#itemFor.
    return completes().with(null);
  }

  @Override
  public <T> Completes<KeyItem<T>> put(final Key key, final Item<T> item) {
    final CompletesEventually completes = completesEventually();
    spaceOf(key).put(key, item).andFinallyConsume(keyItem -> completes.with(keyItem));
    return completes();
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> get(final Key key, final Period until) {
    final CompletesEventually completes = completesEventually();
    spaceOf(key).get(key, until).andFinallyConsume(keyItem -> completes.with(keyItem));
    return completes();
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> take(final Key key, final Period until) {
    final CompletesEventually completes = completesEventually();
    spaceOf(key).take(key, until).andFinallyConsume(keyItem -> completes.with(keyItem));
    return completes();
  }

  private void initialize(final Duration defaultScanInterval) {
    for (int count = 0; count < totalPartitions; ++count) {
      final Definition definition = Definition.has(SpaceActor.class, new SpaceInstantiator(defaultScanInterval), address().name() + "-" + count);
      final Space internalSpace = childActorFor(Space.class, definition);
      partitions[count] = internalSpace;
    }
  }

  private Space spaceOf(final Key key) {
    final int partition = key.hashCode() % totalPartitions;
    return partitions[partition];
  }
}
