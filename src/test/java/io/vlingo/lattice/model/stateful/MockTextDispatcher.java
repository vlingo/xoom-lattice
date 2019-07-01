// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.dispatch.Dispatchable;
import io.vlingo.symbio.store.dispatch.Dispatcher;
import io.vlingo.symbio.store.dispatch.DispatcherControl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MockTextDispatcher implements Dispatcher<Dispatchable<Entry<?>,State<?>>> {
  private AccessSafely access = AccessSafely.afterCompleting(0);

  // public final ConfirmDispatchedResultInterest confirmDispatchedResultInterest;
  public DispatcherControl control;
  public final Map<String,State<?>> dispatched = new HashMap<>();
  private final ConcurrentLinkedQueue<Entry<?>> dispatchedEntries = new ConcurrentLinkedQueue<>();
  public final AtomicBoolean processDispatch = new AtomicBoolean(true);

  public MockTextDispatcher() {
  }

  @Override
  public void controlWith(final DispatcherControl control) {
    this.control = control;
  }

  @Override
  public void dispatch(final Dispatchable<Entry<?>,State<?>> dispatchable) {
    if (processDispatch.get()) {
      access.writeUsing("dispatched", dispatchable.id(), new Dispatch<>(dispatchable.typedState(), dispatchable.entries()));
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely
      .afterCompleting(times)

      .writingWith("dispatched", (String id, Dispatch dispatch) -> { dispatched.put(id, dispatch.state); dispatchedEntries.addAll(dispatch.entries); })

      .readingWith("dispatchedIds", () -> dispatched.keySet())
      .readingWith("dispatchedState", (String id) -> dispatched.get(id))
      .readingWith("dispatchedStateCount", () -> dispatched.size())

      .writingWith("processDispatch", (Boolean flag) -> processDispatch.set(flag))
      .readingWith("processDispatch", () -> processDispatch.get())

      .readingWith("dispatched", () -> dispatched);

    return access;
  }

  private static class Dispatch<S extends State<?>,E extends Entry<?>> {
    final Collection<E> entries;
    final S state;

    Dispatch(final S state, final Collection<E> entries) {
      this.state = state;
      this.entries = entries;
    }
  }
}
