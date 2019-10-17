// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.time.Duration;

public class Lease extends Period {
  public static final Lease Forever = Lease.of(Long.MAX_VALUE);
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
