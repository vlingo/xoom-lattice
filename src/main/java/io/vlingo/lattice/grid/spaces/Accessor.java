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
  private static final long DefaultSweepInterval = 15_000;

  private static final Accessor NullAccessor = new Accessor(null, null);

  private static final Map<String,Accessor> accessors = new ConcurrentHashMap<>();

  public final String name;
  private final Grid grid;
  private final Map<String,Space> spaces = new ConcurrentHashMap<>();

  public static Accessor named(final String name) {
    return accessors.getOrDefault(name, NullAccessor);
  }

  public static synchronized Accessor using(final Grid grid, final String name) {
    Accessor accessor = accessors.putIfAbsent(name, new Accessor(grid, name));
    if (accessor == null) {
      accessor = accessors.get(name);
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
    return spaceFor(name, Duration.ofMillis(DefaultSweepInterval));
  }

  public Space spaceFor(final String name, final long defaultSweepInterval) {
    return spaceFor(name, Duration.ofMillis(defaultSweepInterval));
  }

  public synchronized Space spaceFor(final String name, final Duration defaultSweepInterval) {
    if (defaultSweepInterval.isNegative() || defaultSweepInterval.isZero()) {
      throw new IllegalArgumentException("The defaultSweepInterval must be greater than zero.");
    }

    if (!isDefined()) {
      throw new IllegalStateException("Accessor is invalid.");
    }

    Space space = spaces.get(name);

    if (space == null) {
      final Definition definition = Definition.has(SpaceActor.class, Definition.parameters(defaultSweepInterval), name);
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
