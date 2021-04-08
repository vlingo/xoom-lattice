// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.router;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.common.Completes;

public class SolverActor extends Actor implements Solver {

  @Override
  public Completes<Stuff> solveStuff(final int value) {
    return completes().with(new Stuff(value * 2));
  }
}
