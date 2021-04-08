// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Protocols;
import io.vlingo.xoom.symbio.Entry;
import io.vlingo.xoom.symbio.State;
import io.vlingo.xoom.symbio.store.Result;
import io.vlingo.xoom.symbio.store.dispatch.ConfirmDispatchedResultInterest;
import io.vlingo.xoom.symbio.store.dispatch.Dispatchable;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;
import io.vlingo.xoom.symbio.store.dispatch.DispatcherControl;

public abstract class ProjectionDispatcherActor<E extends Entry<?>, RS extends State<?>> extends AbstractProjectionDispatcherActor
    implements Dispatcher<Dispatchable<E, RS>>, ProjectionDispatcher, ConfirmDispatchedResultInterest {

  private ConfirmDispatchedResultInterest interest;
  private DispatcherControl control;
  private final MultiConfirming multiConfirming;
  private final ProjectionControl multiConfirmingProjectionControl;
  private final ProjectionControl projectionControl;

  protected ProjectionDispatcherActor() {
    this(Arrays.asList(), MultiConfirming.DefaultExpirationLimit);
  }

  protected ProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions, final long multiConfirmationsExpiration) {
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

    final Protocols protocols =
            childActorFor(
                    new Class[] { MultiConfirming.class, ProjectionControl.class },
                    Definition.has(MultiConfirmingProjectionControlActor.class,
                                   Definition.parameters(projectionControl, multiConfirmationsExpiration)));

    this.multiConfirming = protocols.get(0);
    this.multiConfirmingProjectionControl = protocols.get(1);
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
    final List<Projection> projections = projectionsFor(projectable.becauseOf());

    final int count = projections.size();

    if (count > 1) {
      multiConfirming.manageConfirmationsFor(projectable, count);
    }

    for (final Projection projection : projections) {
      projection.projectWith(projectable, count > 1 ? multiConfirmingProjectionControl : projectionControl);
    }
  }
}
