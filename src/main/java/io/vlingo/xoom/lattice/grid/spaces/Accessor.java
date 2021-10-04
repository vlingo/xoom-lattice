// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.lattice.grid.spaces.Space.PartitioningSpaceRouterInstantiator;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Accessor {
  private static final long DefaultScanInterval = 15_000;
  private static final int DefaultTotalPartitions = 5;

  private static final Accessor NullAccessor = new Accessor(null, null);

  public final String name;
  private final Grid grid;
  private final Map<String,Space> spaces = new ConcurrentHashMap<>();
  private final Map<String, DistributedSpace> distributedSpaces = new ConcurrentHashMap<>();

  public static Accessor named(final Grid grid, final String name) {
    Accessor accessor = grid.world().resolveDynamic(name, Accessor.class);

    if (accessor == null) {
      accessor = NullAccessor;
    }

    return accessor;
  }

  public static synchronized Accessor using(final Grid grid, final String name) {
    Accessor accessor = grid.world().resolveDynamic(name, Accessor.class);

    if (accessor == null) {
      accessor = new Accessor(grid, name);
      grid.world().registerDynamic(name, accessor);
    }

    return accessor;
  }

  public boolean isDefined() {
    return grid != null && name != null;
  }

  public boolean isNotDefined() {
    return !isDefined();
  }

  public Space distributedSpaceFor(final String spaceName) {
    return distributedSpaceFor(spaceName, DefaultTotalPartitions, Duration.ofMillis(DefaultScanInterval));
  }

  public Space distributedSpaceFor(final String spaceName, final int totalPartitions) {
    return distributedSpaceFor(spaceName, totalPartitions, Duration.ofMillis(DefaultScanInterval));
  }

  public synchronized Space distributedSpaceFor(final String spaceName, final int totalPartitions, final Duration scanInterval) {
    DistributedSpace distributedSpace = distributedSpaces.get(this.name);
    if (distributedSpace == null) {
      final Stage localStage = grid.localStage();
      final Space localSpace = spaceFor(spaceName, totalPartitions, scanInterval);
      final Definition definition = Definition.has(DistributedSpaceActor.class,
              new DistributedSpace.DistributedSpaceInstantiator(this.name, spaceName, totalPartitions, scanInterval, localSpace, this.grid));
      distributedSpace = localStage.actorFor(DistributedSpace.class, definition);
      distributedSpaces.put(this.name, distributedSpace);
    }

    return distributedSpace;
  }

  public Space spaceFor(final String spaceName) {
    return spaceFor(spaceName, DefaultTotalPartitions, Duration.ofMillis(DefaultScanInterval));
  }

  public Space spaceFor(final String spaceName, final int totalPartitions) {
    return spaceFor(spaceName, totalPartitions, Duration.ofMillis(DefaultScanInterval));
  }

  public Space spaceFor(final String spaceName, final long scanInterval) {
    return spaceFor(spaceName, DefaultTotalPartitions, Duration.ofMillis(scanInterval));
  }

  public Space spaceFor(final String spaceName, final int totalPartitions, final long scanInterval) {
    return spaceFor(spaceName, totalPartitions, Duration.ofMillis(scanInterval));
  }

  public synchronized Space spaceFor(final String spaceName, final int totalPartitions, final Duration scanInterval) {
    if (scanInterval.isNegative() || scanInterval.isZero()) {
      throw new IllegalArgumentException("The defaultScanInterval must be greater than zero.");
    }

    if (!isDefined()) {
      throw new IllegalStateException("Accessor is invalid.");
    }

    Space space = spaces.get(spaceName);

    if (space == null) {
      final Stage localStage = grid.localStage();
      final Definition definition = Definition.has(PartitioningSpaceRouter.class, new PartitioningSpaceRouterInstantiator(totalPartitions, scanInterval), spaceName);
      final Space internalSpace = localStage.actorFor(Space.class, definition);
      space = new SpaceItemFactoryRelay(localStage, internalSpace);
      spaces.put(spaceName, space);
    }

    return space;
  }

  private Accessor(final Grid grid, final String name) {
    this.grid = grid;
    this.name = name;
  }
}
