// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import io.vlingo.xoom.lattice.grid.spaces.ScheduledScanner.ScheduledScannable;

import java.io.Serializable;

public class Item<T> implements ScheduledScannable<Item<T>>, Serializable {
  private static final long serialVersionUID = 2260078842732670754L;

  public final Lease lease;
  public final T object;

  public static <T> Item<T> of(final T object, final Lease lease) {
    return new Item<>(object, lease);
  }

  @Override
  public Item<T> scannable() {
    return this;
  }

  protected Item(final T object, final Lease lease) {
    this.object = object;
    this.lease = lease;
  }
}
