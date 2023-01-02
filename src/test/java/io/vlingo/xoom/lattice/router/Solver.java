// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.router;

import io.vlingo.xoom.common.Completes;

public interface Solver {
  Completes<Stuff> solveStuff(final int value);

  public static final class Stuff {
    public final int value;

    public Stuff(final int value) {
      this.value = value;
    }
  }
}
