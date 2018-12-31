// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.state.StateStore.DispatcherControl;
import io.vlingo.symbio.store.state.TextStateStore.TextDispatcher;

public class MockTextDispatcher implements TextDispatcher {
  // public final ConfirmDispatchedResultInterest confirmDispatchedResultInterest;
  public DispatcherControl control;
  public final Map<String,State<String>> dispatched = new HashMap<>();
  public final AtomicBoolean processDispatch = new AtomicBoolean(true);
  public TestUntil until = TestUntil.happenings(0);

  public MockTextDispatcher() {
  }

  @Override
  public void controlWith(final DispatcherControl control) {
    this.control = control;
  }

  @Override
  public void dispatch(final String dispatchId, final TextState state) {
    if (processDispatch.get()) {
      dispatched.put(dispatchId, state);
      until.happened();
    }
  }

  @Override
  public void dispatchText(final String dispatchId, final TextState state) {
    if (processDispatch.get()) {
      dispatched.put(dispatchId, state);
      until.happened();
    }
  }
}
