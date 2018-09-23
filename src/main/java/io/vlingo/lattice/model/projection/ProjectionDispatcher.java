// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import io.vlingo.actors.Actor;

public interface ProjectionDispatcher {
  void projectTo(final Projection projection, final String becauseOf);

  public static class ProjectToDescription {
    public final Class<? extends Actor> projectionType;
    public final String becauseOf;

    public ProjectToDescription(final Class<? extends Actor> projectionType, final String becauseOf) {
      if (!Projection.class.isAssignableFrom(projectionType)) {
        throw new IllegalArgumentException("Class of projectionType must extend Actor and implement Projection.");
      }
      this.projectionType = projectionType;
      this.becauseOf = becauseOf;
    }
  }
}
