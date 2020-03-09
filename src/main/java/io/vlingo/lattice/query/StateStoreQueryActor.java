// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.query;

import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.common.Completes;
import io.vlingo.common.Outcome;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State.ObjectState;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;

/**
 * A building-block {@code Actor} that queries asynchronously for state by id.
 */
public abstract class StateStoreQueryActor extends Actor implements ReadResultInterest {

  private final ReadResultInterest readInterest;
  private final StateStore stateStore;

  /**
   * Construct my final state with the {@code StateStore}, which must
   * be provided by my concrete extenders.
   * @param stateStore the StateStore from which states are read
   */
  protected StateStoreQueryActor(final StateStore stateStore) {
    this.stateStore = stateStore;
    this.readInterest = selfAs(ReadResultInterest.class);
  }

  /**
   * Answer a {@code Completes<ObjectState<T>>} of the eventual result of querying
   * for the state of the {@code type} and identified by {@code id}.
   *
   * <p>If the state is found, the {@code ObjectState} will contain a valid {@code state}
   * of the {@code type}, the {@code stateVersion}, and {@code metadata}. The contents of
   * the {@code metadata} depends on whether or not it as included in the corresponding
   * {@code write()} operation.
   *
   * <p>If the state is not found, the {@code ObjectState} will be {@code ObjectState#Null}.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param <T> the type of the state
   * @return {@code Completes<ObjectState<T>>}
   */
  @SuppressWarnings("unchecked")
  protected <T> Completes<ObjectState<T>> queryObjectStateFor(final String id, final Class<T> type) {
    return (Completes<ObjectState<T>>) queryFor(id, type, QueryResultHandler.ResultType.ObjectState);
  }

  /**
   * Answer a {@code Completes<T>} of the eventual result of querying
   * for the state of the {@code type} and identified by {@code id}.
   *
   * <p>If the state is found, the outcome is the {@code T} instance.
   *
   * <p>If the state is not found, the outcome is {@code null}.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param <T> the type of the state
   * @return {@code Completes<T>}
   */
  protected <T> Completes<T> queryStateFor(final String id, final Class<T> type) {
    return queryFor(id, type, QueryResultHandler.ResultType.Unwrapped);
  }

  private <T> Completes<T> queryFor(final String id, final Class<T> type, final QueryResultHandler.ResultType resultType) {
    final CompletesEventually completes = completesEventually();
    final Consumer<T> answer = (maybeFoundState) -> completes.with(maybeFoundState);
    stateStore.read(id, type, readInterest, new QueryResultHandler<>(answer, resultType));
    return completes();
  }

  //==================================
  // ReadResultInterest
  //==================================

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  @SuppressWarnings("unchecked")
  final public <S> void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final Metadata metadata, final Object object) {
    outcome.andThen(result -> {
      QueryResultHandler.from(object).completeFoundWith(id, state, stateVersion, metadata);
      return result;
    }).otherwise(cause -> {
      if (cause.result.isNotFound()) {
        QueryResultHandler.from(object).completeNotFound();
      } else {
        logger().info("Query state not read for update because: " + cause.getMessage(), cause);
      }
      return cause.result;
    });
  }

  private static final class QueryResultHandler<T> {
    private enum ResultType { ObjectState, Unwrapped };

    final Consumer<T> consumer;
    final ResultType resultType;

    @SuppressWarnings("rawtypes")
    static QueryResultHandler from(final Object handler) {
      return (QueryResultHandler) handler;
    }

    QueryResultHandler(final Consumer<T> consumer, final ResultType resultType) {
      this.consumer = consumer;
      this.resultType = resultType;
    }

    @SuppressWarnings("unchecked")
    void completeNotFound() {
      switch (resultType) {
      case ObjectState:
        consumer.accept((T) ObjectState.Null);
      case Unwrapped:
        consumer.accept(null);
      }
    }

    @SuppressWarnings("unchecked")
    void completeFoundWith(final String id, final T state, final int stateVersion, final Metadata metadata) {
      switch (resultType) {
      case ObjectState:
        consumer.accept((T) new ObjectState<>(id, Object.class, 1, state, stateVersion, metadata));
        break;
      case Unwrapped:
        consumer.accept(state);
        break;
      }
    }
  }
}
