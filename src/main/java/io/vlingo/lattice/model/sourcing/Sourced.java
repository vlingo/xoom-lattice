// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import io.vlingo.lattice.model.Source;

public abstract class Sourced<T> {
  private static final Map<Class<Sourced<Source<?>>>,Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>>> registeredConsumers = new HashMap<>();

  private final List<Source<T>> applied;
  private final int currentVersion;

  @SuppressWarnings("unchecked")
  public static void registerConsumer(
          Class<? extends Sourced<?>> sourcedType,
          Class<? extends Source<?>> sourceType,
          BiConsumer<? extends Sourced<?>, ? extends Source<?>> consumer) {

    Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(sourcedType);

    if (sourcedTypeMap == null) {
      sourcedTypeMap = new HashMap<>();
      registeredConsumers.put((Class<Sourced<Source<?>>>) sourcedType, sourcedTypeMap);
    }

    sourcedTypeMap.put((Class<Source<?>>) sourceType, (BiConsumer<Sourced<?>, Source<?>>) consumer);
  }

  public List<Source<T>> applied() {
    return applied;
  }

  public int currentVersion() {
    return currentVersion;
  }

  public int nextVersion() {
    return currentVersion + 1;
  }

  protected Sourced() {
    this.applied = new ArrayList<>(1);
    this.currentVersion = 0;
  }

  protected Sourced(final List<Source<T>> stream, final int currentVersion) {
    this.applied = new ArrayList<>(1);
    this.currentVersion = currentVersion;

    final Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(getClass());

    if (sourcedTypeMap == null) {
      throw new IllegalStateException("No such Sourced type.");
    }

    for (final Source<?> source : stream) {
      sourcedTypeMap.get(source.getClass()).accept(this, source);
    }
  }

  @SafeVarargs
  final protected void apply(final Source<T>... sources) {
    final Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(getClass());

    if (sourcedTypeMap == null) {
      throw new IllegalStateException("No such Sourced type.");
    }

    for (final Source<T> source : sources) {
      applied.add(source);
      sourcedTypeMap.get(source.getClass()).accept(this, source);
    }
  }
}
