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
import java.util.stream.Collectors;

import io.vlingo.xoom.symbio.Entry;
import io.vlingo.xoom.symbio.State;
import io.vlingo.xoom.symbio.store.dispatch.Dispatchable;

public class TextProjectionDispatcherActor extends ProjectionDispatcherActor<Entry<?>, State.TextState>
    implements ProjectionDispatcher {

  public TextProjectionDispatcherActor() {
    this(Arrays.asList());
  }

  public TextProjectionDispatcherActor(final Collection<ProjectToDescription> projectToDescriptions) {
    this(projectToDescriptions, MultiConfirming.DefaultExpirationLimit);
  }

  public TextProjectionDispatcherActor(
          final Collection<ProjectToDescription> projectToDescriptions,
          final long multiConfirmationsExpiration) {
    super(projectToDescriptions, multiConfirmationsExpiration);
  }

  @Override
  protected boolean requiresDispatchedConfirmation() {
    return true;
  }

  @Override
  public void dispatch(final Dispatchable<Entry<?>, State.TextState> dispatchable) {
    dispatchable.state().ifPresent(state-> {
      if (hasProjectionsFor(state.metadata.operation)) {
        dispatch(dispatchable.id(), new TextProjectable(state, dispatchable.entries(), dispatchable.id()));
      }
    });

    final List<Entry<?>> entries =
            dispatchable.entries().stream()
              .filter(entry -> hasProjectionsFor(entry.typeName()))
              .collect(Collectors.toList());

    if (!entries.isEmpty()) {
      dispatch(dispatchable.id(), new TextProjectable(dispatchable.state().orElse(null), entries, dispatchable.id()));
    }
  }
}
