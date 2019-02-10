// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import io.vlingo.actors.Actor;

/**
 * Defines the means of dispatching {@code Projectable} instances to {@code Projections}s
 * based on matching the {@code Projections}s that handle descriptive causes.
 */
public interface ProjectionDispatcher {
  /**
   * Use the {@code projection} to project a given {@code Projectable} state when {@code becauseOf} is matched
   * with the reasons of a given .
   * @param projection the Projection that may be used
   * @param becauseOf the String[] holding one or more reasons that projection is required
   */
  void projectTo(final Projection projection, final String[] becauseOf);

  /**
   * Declares the projection type that is dispatched for a given set of causes/reasons.
   */
  public static class ProjectToDescription {
    public final Class<? extends Actor> projectionType;
    public final String[] becauseOf;

    /**
     * Construct my default state.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param becauseOf the String[] causes/reasons that the projectionType handles
     */
    public ProjectToDescription(final Class<? extends Actor> projectionType, final String... becauseOf) {
      if (!Projection.class.isAssignableFrom(projectionType)) {
        throw new IllegalArgumentException("Class of projectionType must extend Actor and implement Projection.");
      }
      this.projectionType = projectionType;
      this.becauseOf = becauseOf;
    }
  }
}
