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
import io.vlingo.actors.Definition;

public class AbstractProjectionDispatcherActor extends Actor implements ProjectionDispatcher {
  private final MatchableProjections matchableProjections;

  protected AbstractProjectionDispatcherActor() {
    this.matchableProjections = new MatchableProjections();
  }

  protected AbstractProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    this();

    for (final ProjectToDescription discription : projectToDescriptions) {
      final Projection projection =
              stage().actorFor(
                      Definition.has(discription.projectionType, Definition.NoParameters),
                      Projection.class);

      projectTo(projection, discription.becauseOf);
    }
  }

  //=====================================
  // ProjectionDispatcher
  //=====================================

  @Override
  public void projectTo(final Projection projection, final String[] whenMatchingCause) {
    matchableProjections.mayDispatchTo(projection, whenMatchingCause);
  }

  //=====================================
  // internal implementation
  //=====================================

  protected boolean hasProjectionsFor(final String actualCause) {
    return !projectionsFor(actualCause).isEmpty();
  }

  protected List<Projection> projectionsFor(final String actualCause) {
    return matchableProjections.matchProjections(actualCause);
  }
}
