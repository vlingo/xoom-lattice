// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.cache;

import io.vlingo.xoom.lattice.grid.hashring.HashedNodePoint;

public class CacheNodePoint<T> extends HashedNodePoint<T> {
  private final Cache cache;

  public CacheNodePoint(final Cache cache, final int hash, final T node) {
    super(hash, node);

    this.cache = cache;
  }

  @Override
  public void excluded() {

  }

  @Override
  public void included() {

  }

  Cache cache() {
    return cache;
  }
}
