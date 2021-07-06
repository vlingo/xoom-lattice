// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.query;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Outcome;
import io.vlingo.xoom.common.Scheduled;
import io.vlingo.xoom.lattice.CompositeIdentitySupport;
import io.vlingo.xoom.reactivestreams.sink.TerminalOperationConsumerSink;
import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.State.ObjectState;
import io.vlingo.xoom.symbio.StateBundle;
import io.vlingo.xoom.symbio.store.Result;
import io.vlingo.xoom.symbio.store.StorageException;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.StateStore.ReadResultInterest;

/**
 * A building-block {@code Actor} that queries asynchronously for state by id.
 */
@SuppressWarnings("rawtypes")
public abstract class StateStoreQueryActor extends Actor implements CompositeIdentitySupport, ReadResultInterest, Scheduled<StateStoreQueryActor.RetryContext> {

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

  @Override
  public void intervalSignal(final Scheduled<RetryContext> scheduled, final RetryContext context) {
    queryWithRetries(context);
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
    return (Completes<ObjectState<T>>) queryFor(id, type, QueryResultHandler.ResultType.ObjectState, (T) ObjectState.Null);
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
   * <p>If the state is not found, the {@code ObjectState} will be {@code notFoundState}.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param notFoundState the T state to answer if the query doesn't find the desired state
   * @param <T> the type of the state
   * @return {@code Completes<ObjectState<T>>}
   */
  @SuppressWarnings("unchecked")
  protected <T> Completes<ObjectState<T>> queryObjectStateFor(final String id, final Class<T> type, final ObjectState<T> notFoundState) {
    return (Completes<ObjectState<T>>) queryFor(id, type, QueryResultHandler.ResultType.ObjectState, (T) notFoundState);
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
   * <p>If the state is not found, the query will be retried up to {@code retryTotal} times in {@code retryInterval} intervals.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param retryInterval the interval for retries if state is not found at first
   * @param retryTotal the maximum number of retries if state is not found at first
   * @param <T> the type of the state
   * @return {@code Completes<ObjectState<T>>}
   */
  protected <T> Completes<ObjectState<T>> queryObjectStateFor(final String id, final Class<T> type, final int retryInterval, final int retryTotal) {
    return queryObjectStateFor(id, type, null, retryInterval, retryTotal);
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
   * <p>If the state is not found, the {@code ObjectState} will be {@code notFoundState}.
   *
   * <p>If the state is not found, the query will be retried up to {@code retryTotal} times in {@code retryInterval} intervals.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param notFoundState the T state to answer if the query doesn't find the desired state
   * @param retryInterval the interval for retries if state is not found at first
   * @param retryTotal the maximum number of retries if state is not found at first
   * @param <T> the type of the state
   * @return {@code Completes<ObjectState<T>>}
   */
  @SuppressWarnings("unchecked")
  protected <T> Completes<ObjectState<T>> queryObjectStateFor(final String id, final Class<T> type, final ObjectState<T> notFoundState, final int retryInterval, final int retryTotal) {
    queryWithRetries(new RetryContext<T>(completesEventually(), (answer) -> queryFor(id, type, QueryResultHandler.ResultType.ObjectState, (T) notFoundState, answer), (T) notFoundState, retryInterval, retryTotal));
    return completes();
  }

  protected <T, R> Completes<Collection<R>> streamAllOf(final Class<T> type, final Collection<R> all) {
    return queryAllOf(type, all);
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
    return queryFor(id, type, QueryResultHandler.ResultType.Unwrapped, null);
  }

  /**
   * Answer a {@code Completes<T>} of the eventual result of querying
   * for the state of the {@code type} and identified by {@code id}.
   *
   * <p>If the state is found, the outcome is the {@code T} instance.
   *
   * <p>If the state is not found, the outcome is {@code notFoundState}.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param notFoundState the T state to answer if the query doesn't find the desired state
   * @param <T> the type of the state
   * @return {@code Completes<T>}
   */
  protected <T> Completes<T> queryStateFor(final String id, final Class<T> type, final T notFoundState) {
    return queryFor(id, type, QueryResultHandler.ResultType.Unwrapped, notFoundState);
  }


  /**
   * Answer a {@code Completes<T>} of the eventual result of querying
   * for the state of the {@code type} identified by {@code id}.
   *
   * <p>If the state is found, the outcome is the {@code T} instance.
   *
   * <p>If the state is not found, the outcome is {@code null}.
   *
   * <p>If the state is not found, the query will be retried up to {@code retryTotal} times in {@code retryInterval} intervals.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param retryInterval the interval for retries if state is not found at first
   * @param retryTotal the maximum number of retries if state is not found at first
   * @param <T> the type of the state
   * @return {@code Completes<T>}
   */
  protected <T> Completes<T> queryStateFor(final String id, final Class<T> type, final int retryInterval, final int retryTotal) {
    return queryStateFor(id, type, null, retryInterval, retryTotal);
  }

  /**
   * Answer a {@code Completes<T>} of the eventual result of querying
   * for the state of the {@code type} identified by {@code id}.
   *
   * <p>If the state is found, the outcome is the {@code T} instance.
   *
   * <p>If the state is not found, the outcome is {@code notFoundState}.
   *
   * <p>If the state is not found, the query will be retried up to {@code retryTotal} times in {@code retryInterval} intervals.
   *
   * @param id the String unique identity of the state to query
   * @param type the {@code Class<T>} type of the state to query
   * @param notFoundState the T state to answer if the query doesn't find the desired state
   * @param retryInterval the interval for retries if state is not found at first
   * @param retryTotal the maximum number of retries if state is not found at first
   * @param <T> the type of the state
   * @return {@code Completes<T>}
   */
  protected <T> Completes<T> queryStateFor(final String id, final Class<T> type, final T notFoundState, final int retryInterval, final int retryTotal) {
    queryWithRetries(new RetryContext<T>(completesEventually(), (answer) -> queryFor(id, type, QueryResultHandler.ResultType.Unwrapped, notFoundState, answer), notFoundState, retryInterval, retryTotal));
    return completes();
  }

  @SuppressWarnings("unchecked")
  private <T> void queryWithRetries(final RetryContext context) {
    final Consumer<T> answer = (maybeFoundState) -> {
      if (context.needsRetry(maybeFoundState)) {
        scheduler().scheduleOnce(selfAs(Scheduled.class), context.nextTry(), 0, context.retryInterval);
      } else {
        context.completes.with(maybeFoundState);
      }
    };
    context.query.accept(answer);
  }

  @SuppressWarnings("unchecked")
  private <T, R> Completes<Collection<R>> queryAllOf(final Class<T> type, final Collection<R> all) {
    final Consumer<StateBundle> populator = (StateBundle state) -> {
      all.add((R) state.object);
    };

    final CompletesEventually completes = completesEventually();
    final Consumer<Collection<R>> collector = (Collection<R> collected) -> {
      completes.with(collected);
    };

    final TerminalOperationConsumerSink sink =
            new TerminalOperationConsumerSink(populator, all, collector);

    stateStore.streamAllOf(type).andFinallyConsume(stream -> stream.flowInto(sink));

    return completes();
  }

  private <T> Completes<T> queryFor(final String id, final Class<T> type, final QueryResultHandler.ResultType resultType, final T notFoundState) {
    final CompletesEventually completes = completesEventually();
    queryFor(id, type, resultType, notFoundState, (T maybeFoundState) -> completes.with(maybeFoundState));
    return completes();
  }

  private <T> void queryFor(final String id, final Class<T> type, final QueryResultHandler.ResultType resultType, final T notFoundState, final Consumer<T> answer) {
    stateStore.read(id, type, readInterest, new QueryResultHandler<>(answer, resultType, notFoundState));
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
    final T notFoundState;
    final ResultType resultType;

    static QueryResultHandler from(final Object handler) {
      return (QueryResultHandler) handler;
    }

    QueryResultHandler(final Consumer<T> consumer, final ResultType resultType, final T notFoundState) {
      this.consumer = consumer;
      this.resultType = resultType;
      this.notFoundState = notFoundState;
    }

    void completeNotFound() {
      consumer.accept(notFoundState);
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

  static final class RetryContext<T> {
    private final CompletesEventually completes;
    private final Consumer<Consumer<T>> query;
    private final T notFoundState;
    private final int retryInterval;
    private final int retriesLeft;

    public RetryContext(final CompletesEventually completes, final Consumer<Consumer<T>> query, final T notFoundState, final int retryInterval, final int retriesLeft) {
      this.completes = completes;
      this.query = query;
      this.notFoundState = notFoundState;
      this.retryInterval = retryInterval;
      this.retriesLeft = retriesLeft;
    }

    @SuppressWarnings("unchecked")
    private RetryContext<T> nextTry() {
      return new RetryContext(completes, query, notFoundState, retryInterval, retriesLeft - 1);
    }

    @SuppressWarnings({ "hiding", "unlikely-arg-type" })
    private <T> boolean needsRetry(final T maybeFoundState) {
      return retriesLeft > 0 && Objects.equals(maybeFoundState, notFoundState);
    }
  }
}
