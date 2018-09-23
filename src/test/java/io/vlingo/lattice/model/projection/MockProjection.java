// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.actors.testkit.TestUntil;

public class MockProjection implements Projection {
  public List<String> projectedDataIds = new ArrayList<>();
  public TestUntil until = TestUntil.happenings(0);

  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    projectedDataIds.add(projectable.dataId());
    control.confirmProjected(projectable.projectionId());
    until.happened();
  }
}
