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
import io.vlingo.symbio.State.BinaryState;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

public abstract class StatefulEntity<S,R extends State<?>> extends Actor
    implements Stateful<S>, ReadResultInterest<R>, WriteResultInterest<R> {

  private final Info<S,R> info;

  @Override
  @SuppressWarnings("unchecked")
  public void preserve(final S state, final String metadataValue, final String operation, final BiConsumer<S,Integer> consumer) {
    final Metadata metadata = Metadata.with(state, metadataValue == null ? "" : metadataValue, operation == null ? "" : operation);
    final R raw = info.adapter.toRawState(state, stateVersion() + 1, metadata);
    if (info.isBinary()) {
      stowMessages(WriteResultInterest.class);
      info.binaryStateStore().write((BinaryState) raw, selfAs(WriteResultInterest.class), consumer);
    } else {
      stowMessages(WriteResultInterest.class);
      info.textStateStore().write((TextState) raw, selfAs(WriteResultInterest.class), consumer);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void restore(final BiConsumer<S,Integer> consumer) {
    if (info.isBinary()) {
      stowMessages(ReadResultInterest.class);
      info.binaryStateStore().read(id(), (Class<S>) stateType(), selfAs(ReadResultInterest.class), consumer);
    } else {
      stowMessages(ReadResultInterest.class);
      info.textStateStore().read(id(), (Class<S>) stateType(), selfAs(ReadResultInterest.class), consumer);
    }
  }

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  @SuppressWarnings("unchecked")
  final public void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final R state, final Object consumer) {
    outcome
      .andThen(result -> {
        final S preserved = info.adapter.fromRawState(state);
        if (consumer != null) {
          ((BiConsumer<S,Integer>) consumer).accept(preserved, state.dataVersion);
        } else {
          state(preserved, state.dataVersion);
        }
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "State not restored for: " + state.type + "(" + id + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().log(message, cause);
        throw new IllegalStateException(message, cause);
      });
  }

  @Override
  @SuppressWarnings("unchecked")
  final public void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final R state, final Object consumer) {
    outcome
      .andThen(result -> {
        final S preserved = info.adapter.fromRawState(state);
        if (consumer != null) {
          ((BiConsumer<S,Integer>) consumer).accept(preserved, state.dataVersion);
        } else {
          state(preserved, state.dataVersion);
        }
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "State not preserved for: " + state.type + "(" + id + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().log(message, cause);
        throw new IllegalStateException(message, cause);
      });
  }

  protected StatefulEntity() {
    this.info = stage().world().resolveDynamic(StatefulTypeRegistry.INTERNAL_NAME, StatefulTypeRegistry.class).info(stateType());
  }
}
