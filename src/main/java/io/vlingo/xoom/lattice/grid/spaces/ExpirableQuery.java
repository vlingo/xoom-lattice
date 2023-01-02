// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import java.time.Instant;

import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.lattice.grid.spaces.ScheduledScanner.ScheduledScannable;

class ExpirableQuery implements ScheduledScannable<ExpirableQuery>, Comparable<ExpirableQuery> {
  final CompletesEventually completes;
  final Instant expiresOn;
  final Key key;
  final Period period;
  final boolean retainItem;

  ExpirableQuery(final Key key, final boolean retainItem, final Instant expiresOn, final Period period, final CompletesEventually completes) {
    this.key = key;
    this.retainItem = retainItem;
    this.expiresOn = expiresOn;
    this.period = period;
    this.completes = completes;
  }

  boolean isMaximumExpiration() {
    return expiresOn.getEpochSecond() == Instant.MAX.getEpochSecond();
  }

  @Override
  public ExpirableQuery scannable() {
    return this;
  }

  @Override
  public int compareTo(final ExpirableQuery other) {
    return key.compare(key, other.key);
  }
}
