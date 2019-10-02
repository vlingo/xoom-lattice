// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.dispatch.Dispatchable;

public class BinaryProjectionDispatcherActor extends ProjectionDispatcherActor<Entry<?>, State<byte[]>>
    implements ProjectionDispatcher {

  public BinaryProjectionDispatcherActor() {
    super();
  }

  public BinaryProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    super(projectToDescriptions);
  }

  @Override
  protected boolean requiresDispatchedConfirmation() {
    return true;
  }

  @Override
  public void dispatch(final Dispatchable<Entry<?>, State<byte[]>> dispatchable) {
    dispatchable.state().ifPresent(state-> {
      if (hasProjectionsFor(state.metadata.operation)) {
        dispatch(dispatchable.id(), new BinaryProjectable(state, dispatchable.entries(), dispatchable.id()));
      }
    });

    final List<Entry<?>> entries =
            dispatchable.entries().stream()
              .filter(entry -> hasProjectionsFor(entry.typeName()))
              .collect(Collectors.toList());

    if (!entries.isEmpty()) {
      dispatch(dispatchable.id(), new BinaryProjectable(dispatchable.state().orElse(null), entries, dispatchable.id()));
    }
  }
}
