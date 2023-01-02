// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public class Period implements Serializable {
  private static final long serialVersionUID = 9024915819552469634L;

  public static final Period Forever = Period.of(Instant.MAX.getEpochSecond());
  public static final Period None = Period.of(0);


  public final Duration duration;

  public static Period of(final Duration duration) {
    return new Period(duration);
  }

  public static Period of(final long duration) {
    return new Period(Duration.ofMillis(duration));
  }

  public boolean isForever() {
    return toMilliseconds() == Forever.toMilliseconds();
  }

  public Instant toFutureInstant() {
    return isForever() ? Instant.MAX : Instant.now().plus(duration);
  }

  public long toMilliseconds() {
    return duration.toMillis();
  }

  protected Period(final Duration duration) {
    this.duration = duration;
  }
}
