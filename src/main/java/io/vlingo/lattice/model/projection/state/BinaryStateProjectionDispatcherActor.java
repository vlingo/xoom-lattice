// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import java.util.Collection;

import io.vlingo.lattice.model.projection.ProjectionDispatcher;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.state.StateStore.Dispatcher;

public class BinaryStateProjectionDispatcherActor extends StateProjectionDispatcherActor
    implements ProjectionDispatcher, Dispatcher {

  public BinaryStateProjectionDispatcherActor() {
    super();
  }

  public BinaryStateProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    super(projectToDescriptions);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S extends State<?>, E extends Entry<?>> void dispatch(final String dispatchId, final S state, final Collection<E> entries) {
    if (hasProjectionsFor(state.metadata.operation)) {
      dispatch(dispatchId, new ProjectableBinaryState((State<byte[]>) state, (Collection<Entry<?>>) entries, dispatchId));
    }
  }

  @Override
  protected boolean requiresDispatchedConfirmation() {
    return true;
  }
}
