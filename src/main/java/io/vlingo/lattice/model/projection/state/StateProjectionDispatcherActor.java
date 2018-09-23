// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import java.util.Arrays;
import java.util.Collection;

import io.vlingo.lattice.model.projection.AbstractProjectionDispatcherActor;
import io.vlingo.lattice.model.projection.Projectable;
import io.vlingo.lattice.model.projection.Projection;
import io.vlingo.lattice.model.projection.ProjectionControl;
import io.vlingo.lattice.model.projection.ProjectionDispatcher;
import io.vlingo.symbio.store.state.StateStore.ConfirmDispatchedResultInterest;
import io.vlingo.symbio.store.state.StateStore.Dispatcher;
import io.vlingo.symbio.store.state.StateStore.DispatcherControl;
import io.vlingo.symbio.store.state.StateStore.Result;

public abstract class StateProjectionDispatcherActor extends AbstractProjectionDispatcherActor
    implements Dispatcher, ProjectionDispatcher, ConfirmDispatchedResultInterest {

  private ConfirmDispatchedResultInterest interest;
  private DispatcherControl control;
  private final ProjectionControl projectionControl;

  protected StateProjectionDispatcherActor() {
    this(Arrays.asList());
  }

  protected StateProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    super(projectToDescriptions);

    this.interest = selfAs(ConfirmDispatchedResultInterest.class);
    this.projectionControl = new ProjectionControl() {
      @Override
      public void confirmProjected(String projectionId) {
        if (control != null) {
          control.confirmDispatched(projectionId, interest);
        } else if (requiresDispatchedConfirmation()) {
          logger().log("WARNING: ProjectionDispatcher control is not set; unconfirmed: " + projectionId);
        }
      }
    };
  }

  //=====================================
  // Dispatcher
  //=====================================

  @Override
  public void controlWith(final DispatcherControl control) {
    this.control = control;
  }

  //=====================================
  // ConfirmDispatchedResultInterest
  //=====================================

  @Override
  public void confirmDispatchedResultedIn(final Result result, final String dispatchId) { }

  //=====================================
  // internal implementation
  //=====================================

  protected abstract boolean requiresDispatchedConfirmation();

  protected void dispatch(final String dispatchId, final Projectable projectable) {
    for (final Projection projection : projectionsFor(projectable.becauseOf())) {
      projection.projectWith(projectable, projectionControl);
    }
  }
}
