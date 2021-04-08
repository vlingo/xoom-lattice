// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.lattice.model.projection.WarbleStateStoreProjection.Warble;
import io.vlingo.xoom.symbio.store.state.StateStore;

public class WarbleStateStoreProjection extends StateStoreProjectionActor<Warble> {
  private final boolean alwaysWrite;

  public WarbleStateStoreProjection(final StateStore stateStore) {
    this(stateStore, true);
  }

  public WarbleStateStoreProjection(final StateStore stateStore, final boolean alwaysWrite) {
    super(stateStore);

    this.alwaysWrite = alwaysWrite;
  }

  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    upsertFor(projectable, control);
  }

  @Override
  protected boolean alwaysWrite() {
    return alwaysWrite;
  }

  @Override
  protected Warble currentDataFor(final Projectable projectable) {
    return projectable.object();
  }

  @Override
  protected Warble merge(final Warble previousData, final int previousVersion, final Warble currentData, final int currentVersion) {
    if (previousData == null) {
      return currentData;
    }

    // special case for test:
    // when !alwaysWrite answer one of two values:
    // 1. if currentData.count == 1_000 && previousData.count == 1_000
    //      answer previousData to force equals comparison
    // 2. if currentData.count != previousData.count
    //      answer currentData to force !equals comparison
    //
    if (!alwaysWrite) {
      if (currentData.count == 1_000 && previousData.count == 1_000) {
        return previousData;
      }
    }

    return new Warble(currentData.name, currentData.type, currentData.count + previousData.count);
  }

  public static class Warble {
    private static final AtomicInteger nextDataVersion = new AtomicInteger(0);

    public final int count;
    public final String name;
    public final String type;
    public final int version;

    public Warble(final String name, final String type, final int count) {
      this.name = name;
      this.type = type;
      this.count = count;
      this.version = nextDataVersion.incrementAndGet();
    }
  }
}
