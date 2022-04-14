// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import java.time.Duration;
import java.time.Instant;

public class Lease extends Period {
  private static final long serialVersionUID = -1198289722304469050L;

  public static final Lease Forever = Lease.of(Instant.MAX.getEpochSecond());
  public static final Lease None = Lease.of(0);

  public static Lease of(final Duration duration) {
    return new Lease(duration);
  }

  public static Lease of(final long period) {
    return new Lease(Duration.ofMillis(period));
  }

  private Lease(final Duration duration) {
    super(duration);
  }
}
