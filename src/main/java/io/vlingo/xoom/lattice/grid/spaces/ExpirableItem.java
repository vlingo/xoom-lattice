// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import java.time.Instant;

class ExpirableItem<T> implements Comparable<ExpirableItem<T>> {
  final Key key;
  final T object;
  final Instant expiresOn;
  final Lease lease;

  ExpirableItem(final Key key, final T object, final Instant expiresOn, final Lease lease) {
    this.key = key;
    this.object = object;
    this.expiresOn = expiresOn;
    this.lease = lease;
  }

  boolean isMaximumExpiration() {
    return expiresOn.getEpochSecond() == Instant.MAX.getEpochSecond();
  }

  @Override
  public int compareTo(final ExpirableItem<T> other) {
    return key.compare(key, other.key);
  }
}
