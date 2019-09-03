// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.dispatch.ConfirmDispatchedResultInterest;
import io.vlingo.symbio.store.dispatch.Dispatchable;
import io.vlingo.symbio.store.dispatch.Dispatcher;
import io.vlingo.symbio.store.dispatch.DispatcherControl;

import java.util.Arrays;
import java.util.Collection;

public abstract class ProjectionDispatcherActor<S extends State<?>> extends AbstractProjectionDispatcherActor
    implements Dispatcher<Dispatchable<Entry<?>, S>>, ProjectionDispatcher, ConfirmDispatchedResultInterest {

  private ConfirmDispatchedResultInterest interest;
  private DispatcherControl control;
  private final ProjectionControl projectionControl;

  protected ProjectionDispatcherActor() {
    this(Arrays.asList());
  }

  protected ProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    super(projectToDescriptions);

    this.interest = selfAs(ConfirmDispatchedResultInterest.class);
    this.projectionControl = new ProjectionControl() {
      @Override
      public void confirmProjected(String projectionId) {
        if (control != null) {
          control.confirmDispatched(projectionId, interest);
        } else if (requiresDispatchedConfirmation()) {
          logger().error("WARNING: ProjectionDispatcher control is not set; unconfirmed: " + projectionId);
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
