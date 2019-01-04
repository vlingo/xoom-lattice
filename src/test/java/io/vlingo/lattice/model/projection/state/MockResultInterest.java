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
import io.vlingo.common.Outcome;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore.ConfirmDispatchedResultInterest;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

public class MockResultInterest
    implements ReadResultInterest,
               WriteResultInterest,
               ConfirmDispatchedResultInterest {

  public AtomicInteger confirmDispatchedResultedIn = new AtomicInteger(0);
  public AtomicInteger readTextResultedIn = new AtomicInteger(0);
  public AtomicInteger writeTextResultedIn = new AtomicInteger(0);
  public TestUntil until;

  public AtomicReference<Result> textReadResult = new AtomicReference<>();
  public AtomicReference<Result> textWriteResult = new AtomicReference<>();
  public ConcurrentLinkedQueue<Result> textWriteAccumulatedResults = new ConcurrentLinkedQueue<>();
  public AtomicReference<Object> stateHolder = new AtomicReference<>();
  public AtomicReference<Metadata> metadataHolder = new AtomicReference<>();
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
  public <S> void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final Metadata metadata, final Object object) {
    outcome
      .andThen(result -> {
        readTextResultedIn.incrementAndGet();
        textReadResult.set(result);
        stateHolder.set(state);
        metadataHolder.set(metadata);
        until.happened();
        return result;
      })
      .otherwise(cause -> {
        readTextResultedIn.incrementAndGet();
        textReadResult.set(cause.result);
        stateHolder.set(state);
        metadataHolder.set(metadata);
        errorCauses.add(cause);
        until.happened();
        return cause.result;
      });
  }


  @Override
  public <S> void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final Object object) {
    outcome
      .andThen(result -> {
        writeTextResultedIn.incrementAndGet();
        textWriteResult.set(result);
        textWriteAccumulatedResults.add(result);
        stateHolder.set(state);
        until.happened();
        return result;
      })
      .otherwise(cause -> {
        writeTextResultedIn.incrementAndGet();
        textWriteResult.set(cause.result);
        textWriteAccumulatedResults.add(cause.result);
        stateHolder.set(state);
        errorCauses.add(cause);
        until.happened();
        return cause.result;
      });
  }
}
