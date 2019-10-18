// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.actors.Definition;
import io.vlingo.lattice.grid.Grid;

public class Accessor {
  private static final long DefaultScanInterval = 15_000;
  private static final int DefaultTotalPartitions = 5;

  private static final Accessor NullAccessor = new Accessor(null, null);

  public final String name;
  private final Grid grid;
  private final Map<String,Space> spaces = new ConcurrentHashMap<>();

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

  public Space spaceFor(final String name) {
    return spaceFor(name, DefaultTotalPartitions, Duration.ofMillis(DefaultScanInterval));
  }

  public Space spaceFor(final String name, final int totalPartitions) {
    return spaceFor(name, totalPartitions, Duration.ofMillis(DefaultScanInterval));
  }

  public Space spaceFor(final String name, final long defaultScanInterval) {
    return spaceFor(name, DefaultTotalPartitions, Duration.ofMillis(defaultScanInterval));
  }

  public Space spaceFor(final String name, final int totalPartitions, final long defaultScanInterval) {
    return spaceFor(name, totalPartitions, Duration.ofMillis(defaultScanInterval));
  }

  public synchronized Space spaceFor(final String name, final int totalPartitions, final Duration defaultScanInterval) {
    if (defaultScanInterval.isNegative() || defaultScanInterval.isZero()) {
      throw new IllegalArgumentException("The defaultScanInterval must be greater than zero.");
    }

    if (!isDefined()) {
      throw new IllegalStateException("Accessor is invalid.");
    }

    Space space = spaces.get(name);

    if (space == null) {
      final Definition definition = Definition.has(PartitioningSpaceRouter.class, Definition.parameters(totalPartitions, defaultScanInterval), name);
      final Space internalSpace = grid.actorFor(Space.class, definition);
      space = new SpaceItemFactoryRelay(grid, internalSpace);
      spaces.put(name, space);
    }

    return space;
  }

  private Accessor(final Grid grid, final String name) {
    this.grid = grid;
    this.name = name;
  }
}
