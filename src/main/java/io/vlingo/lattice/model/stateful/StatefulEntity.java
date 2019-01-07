// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import java.util.function.Supplier;

import io.vlingo.actors.Actor;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.CompletionSupplier;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

public abstract class StatefulEntity<S,R extends State<?>> extends Actor
    implements Stateful<S>, ReadResultInterest, WriteResultInterest {

  private int currentVersion;
  private final Info<S,R> info;
  private final ReadResultInterest readInterest;
  private final WriteResultInterest writeInterest;

  @Override
  public <RT> void preserve(final S state, final String metadataValue, final String operation, final Supplier<RT> andThen) {
    final Metadata metadata = Metadata.with(state, metadataValue == null ? "" : metadataValue, operation == null ? "" : operation);
    stowMessages(WriteResultInterest.class);
    info.store.write(id(), state, nextVersion(), metadata, writeInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  @Override
  public void restore() {
    stowMessages(ReadResultInterest.class);
    info.store.read(id(), (Class<S>) stateType(), readInterest);
  }

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  @SuppressWarnings("unchecked")
  final public <ST> void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final ST state, final int stateVersion, final Metadata metadata, final Object object) {
    outcome
      .andThen(result -> {
        state((S) state);
        currentVersion = stateVersion;
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "State not restored for: " + getClass() + "(" + id + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().log(message, cause);
        throw new IllegalStateException(message, cause);
      });
  }

  @Override
  @SuppressWarnings("unchecked")
  final public <ST> void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final ST state, final int stateVersion, final Object supplier) {
    outcome
    .andThen(result -> {
      state((S) state);
      currentVersion = stateVersion;
      completeUsing(supplier);
      disperseStowedMessages();
      return result;
    })
    .otherwise(cause -> {
      final String message = "State not preserved for: " + getClass() + "(" + id + ") because: " + cause.result + " with: " + cause.getMessage();
      logger().log(message, cause);
      throw new IllegalStateException(message, cause);
    });
  }

  protected StatefulEntity() {
    this.currentVersion = 0;
    this.info = stage().world().resolveDynamic(StatefulTypeRegistry.INTERNAL_NAME, StatefulTypeRegistry.class).info(stateType());
    this.readInterest = selfAs(ReadResultInterest.class);
    this.writeInterest = selfAs(WriteResultInterest.class);
  }

  /**
   * Answer my currentVersion, which, if zero, indicates that the
   * receiver is being initially constructed or reconstituted.
   * @return int
   */
  protected int currentVersion() {
    return currentVersion;
  }

  private void completeUsing(final Object supplier) {
    if (supplier != null) {
      ((CompletionSupplier<?>) supplier).complete();
    }
  }

  /**
   * Answer my nextVersion, which is one greater than my currentVersion.
   * @return int
   */
  private int nextVersion() {
    return currentVersion + 1;
  }
}
