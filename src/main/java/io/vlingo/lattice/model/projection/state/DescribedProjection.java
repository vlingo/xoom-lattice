// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.lattice.model.projection.Projectable;
import io.vlingo.lattice.model.projection.Projection;
import io.vlingo.lattice.model.projection.ProjectionControl;
import io.vlingo.symbio.store.state.StateStore.ConfirmDispatchedResultInterest;
import io.vlingo.symbio.store.state.StateStore.DispatcherControl;

public class DescribedProjection extends Actor implements Projection {

  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    control.confirmProjected(projectable.projectionId());
  }

  public static final class Outcome implements DispatcherControl {
    public final AtomicInteger count;
    public final TestUntil until;

    public Outcome(final int testUntilHappenings) {
      this.count = new AtomicInteger(0);
      this.until = TestUntil.happenings(testUntilHappenings);
    }

    @Override
    public void confirmDispatched(final String dispatchId, final ConfirmDispatchedResultInterest interest) {
      count.getAndIncrement();
      until.happened();
    }

    @Override
    public void dispatchUnconfirmed() { }
  }
}
