// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.time.Instant;

public class ExpirableItem<T> {
  public final Key key;
  public final T object;
  public final Instant expiresOn;

  public ExpirableItem(final Key key, final T object, final Instant expiresOn) {
    this.key = key;
    this.object = object;
    this.expiresOn = expiresOn;
  }

  public boolean isMaximumExpiration() {
    return expiresOn.toEpochMilli() == Long.MAX_VALUE;
  }
}
