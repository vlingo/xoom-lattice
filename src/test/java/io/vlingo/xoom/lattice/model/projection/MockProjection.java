// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.actors.testkit.AccessSafely;

public class MockProjection implements Projection {
  public List<String> projectedDataIds = new ArrayList<>();
  public AccessSafely access = AccessSafely.afterCompleting(0);
  private AtomicInteger projections = new AtomicInteger(0);

  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    access.writeUsing("projections", 1, projectable.dataId());
  }

  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely.afterCompleting(times);

    access
      .writingWith("projections", (Integer val, String id) -> {
        projections.set(projections.get() + val);
        projectedDataIds.add(id);
      })
      .readingWith("projections", () -> projections.get())
      .readingWith("projectionId", (Integer index) -> projectedDataIds.get(index));

    return access;
  }
}
