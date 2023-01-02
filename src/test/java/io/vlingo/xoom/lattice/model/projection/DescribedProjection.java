// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.symbio.store.dispatch.ConfirmDispatchedResultInterest;
import io.vlingo.xoom.symbio.store.dispatch.DispatcherControl;

public class DescribedProjection extends Actor implements Projection {

  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    control.confirmProjected(projectable.projectionId());
  }

  public static final class Outcome implements DispatcherControl {
    public final AtomicInteger count;
    private AccessSafely access = AccessSafely.afterCompleting(0);

    public Outcome(final int testUntilHappenings) {
      this.count = new AtomicInteger(0);
      this.access = AccessSafely.afterCompleting(testUntilHappenings);
    }

    @Override
    public void confirmDispatched(final String dispatchId, final ConfirmDispatchedResultInterest interest) {
      access.writeUsing("count", 1);
    }

    @Override
    public void dispatchUnconfirmed() { }

    @Override
    public void stop() { }

    public AccessSafely afterCompleting(final int times) {
      access = AccessSafely.afterCompleting(times);
      access
        .writingWith("count", (Integer increment) -> count.addAndGet(increment))
        .readingWith("count", () -> count.get());

      return access;
    }
  }
}
