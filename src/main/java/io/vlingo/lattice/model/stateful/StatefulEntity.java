// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import java.util.function.BiConsumer;

import io.vlingo.actors.Actor;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

public abstract class StatefulEntity<S,R extends State<?>> extends Actor
    implements Stateful<S>, ReadResultInterest, WriteResultInterest {

  private final Info<S,R> info;

  @Override
  public void preserve(final S state, final String metadataValue, final String operation, final BiConsumer<S,Integer> consumer) {
    final Metadata metadata = Metadata.with(state, metadataValue == null ? "" : metadataValue, operation == null ? "" : operation);
    stowMessages(WriteResultInterest.class);
    info.store.write(id(), state, stateVersion() + 1, metadata, selfAs(WriteResultInterest.class), consumer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void restore(final BiConsumer<S,Integer> consumer) {
    stowMessages(ReadResultInterest.class);
    info.store.read(id(), (Class<S>) stateType(), selfAs(ReadResultInterest.class), consumer);
  }

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <ST> void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final ST state, final int stateVersion, final Metadata metadata, Object consumer) {
    outcome
      .andThen(result -> {
        if (consumer != null) {
          ((BiConsumer<ST,Integer>) consumer).accept(state, stateVersion);
        } else {
          state((S) state, stateVersion);
        }
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
  public <ST> void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final ST state, final int stateVersion, final Object consumer) {
    outcome
    .andThen(result -> {
      if (consumer != null) {
        ((BiConsumer<ST,Integer>) consumer).accept(state, stateVersion);
      } else {
        state((S) state, stateVersion);
      }
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
    this.info = stage().world().resolveDynamic(StatefulTypeRegistry.INTERNAL_NAME, StatefulTypeRegistry.class).info(stateType());
  }
}
