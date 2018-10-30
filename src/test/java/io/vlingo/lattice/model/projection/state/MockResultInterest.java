// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.state.StateStore.ConfirmDispatchedResultInterest;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

public class MockResultInterest
    implements ReadResultInterest<String>,
               WriteResultInterest<String>,
               ConfirmDispatchedResultInterest {

  public AtomicInteger confirmDispatchedResultedIn = new AtomicInteger(0);
  public AtomicInteger readTextResultedIn = new AtomicInteger(0);
  public AtomicInteger writeTextResultedIn = new AtomicInteger(0);
  public TestUntil until;

  public AtomicReference<Result> textReadResult = new AtomicReference<>();
  public AtomicReference<Result> textWriteResult = new AtomicReference<>();
  public ConcurrentLinkedQueue<Result> textWriteAccumulatedResults = new ConcurrentLinkedQueue<>();
  public AtomicReference<State<String>> textState = new AtomicReference<>();
  public ConcurrentLinkedQueue<Exception> errorCauses = new ConcurrentLinkedQueue<>();

  public MockResultInterest(final int testUntilHappenings) {
    until = TestUntil.happenings(testUntilHappenings);
  }

  @Override
  public void confirmDispatchedResultedIn(final Result result, final String dispatchId) {
    confirmDispatchedResultedIn.incrementAndGet();
    until.happened();
  }

  @Override
  public void readResultedIn(final Result result, final String id, final State<String> state, final Object object) {
    readTextResultedIn.incrementAndGet();
    textReadResult.set(result);
    textState.set(state);
    until.happened();
  }

  @Override
  public void readResultedIn(final Result result, final Exception cause, final String id, final State<String> state, final Object object) {
    readTextResultedIn.incrementAndGet();
    textReadResult.set(result);
    textState.set(state);
    errorCauses.add(cause);
    until.happened();
  }

  @Override
  public void writeResultedIn(final Result result, final String id, final State<String> state, final Object object) {
    writeTextResultedIn.incrementAndGet();
    textWriteResult.set(result);
    textWriteAccumulatedResults.add(result);
    textState.set(state);
    until.happened();
  }

  @Override
  public void writeResultedIn(final Result result, final Exception cause, final String id, final State<String> state, final Object object) {
    writeTextResultedIn.incrementAndGet();
    textWriteResult.set(result);
    textWriteAccumulatedResults.add(result);
    textState.set(state);
    errorCauses.add(cause);
    until.happened();
  }
}
