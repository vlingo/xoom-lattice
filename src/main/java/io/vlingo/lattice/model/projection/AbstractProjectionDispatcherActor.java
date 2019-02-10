// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.Collection;
import java.util.List;

import io.vlingo.actors.Actor;

/**
 * Abstract base of all {@code ProjectionDispatcher} types and that
 * holds the pool of {@code Projection} instances that are used to
 * project {@code Projectable} states based on {@code MatchableProjections}.
 */
public class AbstractProjectionDispatcherActor extends Actor implements ProjectionDispatcher {
  private final MatchableProjections matchableProjections;

  /**
   * Construct my default state.
   */
  protected AbstractProjectionDispatcherActor() {
    this.matchableProjections = new MatchableProjections();
  }

  /**
   * Construct my default state with {@code projectToDescriptions}.
   * @param projectToDescriptions the {@code Collection<ProjectToDescription>} describing my matchable projections
   */
  protected AbstractProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    this();

    for (final ProjectToDescription discription : projectToDescriptions) {
      final Projection projection = stage().actorFor(Projection.class, discription.projectionType);

      projectTo(projection, discription.becauseOf);
    }
  }

  //=====================================
  // ProjectionDispatcher
  //=====================================

  /*
   * @see io.vlingo.lattice.model.projection.ProjectionDispatcher#projectTo(io.vlingo.lattice.model.projection.Projection, java.lang.String[])
   */
  @Override
  public void projectTo(final Projection projection, final String[] whenMatchingCause) {
    matchableProjections.mayDispatchTo(projection, whenMatchingCause);
  }

  //=====================================
  // internal implementation
  //=====================================

  /**
   * Answer whether or not I have any {@code Projection} that supports the {@code actualCause}.
   * @param actualCause the String describing the cause that requires Projection
   * @return boolean
   */
  protected boolean hasProjectionsFor(final String actualCause) {
    return !projectionsFor(actualCause).isEmpty();
  }

  /**
   * Answer the {@code List<Projection>} of my {@code Projection} that match {@code actualCause}.
   * @param actualCause the String describing the cause that requires Projection
   * @return {@code List<Projection>}
   */
  protected List<Projection> projectionsFor(final String actualCause) {
    return matchableProjections.matchProjections(actualCause);
  }
}
