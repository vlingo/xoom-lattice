// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.lattice.model.projection.WarbleStateStoreProjection.Warble;
import io.vlingo.symbio.store.state.StateStore;

public class WarbleStateStoreProjection extends StateStoreProjectionActor<Warble> {

  public WarbleStateStoreProjection(final StateStore stateStore) {
    super(stateStore);
  }

  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    upsertFor(projectable, control);
  }

  @Override
  protected Warble currentDataFor(final Projectable projectable) {
    return projectable.object();
  }

  @Override
  protected Warble merge(final Warble previousData, final int previousVersion, final Warble currentData, final int currentVersion) {
    return currentData;
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
