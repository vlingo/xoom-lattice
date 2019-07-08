// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import io.vlingo.lattice.model.projection.ProjectionDispatcher;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.dispatch.Dispatchable;

import java.util.Collection;

public class BinaryStateProjectionDispatcherActor extends StateProjectionDispatcherActor<State<byte[]>>
    implements ProjectionDispatcher {

  public BinaryStateProjectionDispatcherActor() {
    super();
  }

  public BinaryStateProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    super(projectToDescriptions);
  }
  
  @Override
  protected boolean requiresDispatchedConfirmation() {
    return true;
  }

  @Override
  public void dispatch(final Dispatchable<Entry<?>, State<byte[]>> dispatchable) {
    dispatchable.state().ifPresent(state -> {
      if (hasProjectionsFor(state.metadata.operation)) {
        dispatch(dispatchable.id(), new ProjectableBinaryState(state, dispatchable.entries(), dispatchable.id()));
      }
    });
  }
}
