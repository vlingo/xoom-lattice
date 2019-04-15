// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.common.Completes;

public interface Solver {
  Completes<Stuff> solveStuff(final int value);

  public static final class Stuff {
    public final int value;

    public Stuff(final int value) {
      this.value = value;
    }
  }
}
