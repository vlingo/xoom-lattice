// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import java.util.Collection;

import io.vlingo.lattice.model.projection.ProjectionDispatcher;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.state.BinaryStateStore.BinaryDispatcher;

public class BinaryStateProjectionDispatcherActor extends StateProjectionDispatcherActor
    implements ProjectionDispatcher, BinaryDispatcher {

  public BinaryStateProjectionDispatcherActor() {
    super();
  }

  public BinaryStateProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    super(projectToDescriptions);
  }

  @Override
  public void dispatchBinary(final String dispatchId, final State<byte[]> state) {
    if (hasProjectionsFor(state.metadata.operation)) {
      dispatch(dispatchId, new ProjectableBinaryState(state, dispatchId));
    }
  }

  @Override
  protected boolean requiresDispatchedConfirmation() {
    return true;
  }
}
