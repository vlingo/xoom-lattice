// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.query;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.CompletionTranslator;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Outcome;
import io.vlingo.xoom.lattice.query.QueryAttempt.Cardinality;
import io.vlingo.xoom.symbio.store.QueryExpression;
import io.vlingo.xoom.symbio.store.Result;
import io.vlingo.xoom.symbio.store.StorageException;
import io.vlingo.xoom.symbio.store.object.ObjectStore;
import io.vlingo.xoom.symbio.store.object.ObjectStoreReader.QueryMultiResults;
import io.vlingo.xoom.symbio.store.object.ObjectStoreReader.QueryResultInterest;
import io.vlingo.xoom.symbio.store.object.ObjectStoreReader.QuerySingleResult;

/**
 * An building-block {@code Actor} that queries asynchronously and provides a translated outcome.
 */
public abstract class StateObjectQueryActor extends Actor implements QueryResultInterest {
  private final ObjectStore objectStore;
  private final QueryResultInterest queryResultInterest;

  /**
   * Construct my state.
   * @param objectStore the ObjectStore the query
   */
  protected StateObjectQueryActor(final ObjectStore objectStore) {
    this.objectStore = objectStore;
    this.queryResultInterest = selfAs(QueryResultInterest.class);
  }

  /**
   * Answer {@code Optional<QueryFailedException>} that should be thrown
   * and handled by my {@code Supervisor}, unless it is empty. The default
   * behavior is to answer the given {@code exception}, which will be thrown.
   * Must override to change default behavior.
   * @param exception the QueryFailedException
   * @return {@code Optional<QueryFailedException>}
   */
  protected Optional<ObjectQueryFailedException> afterQueryFailed(final ObjectQueryFailedException exception) {
    return Optional.of(exception);
  }

  /**
   * Answer the {@code Completes<R>} through which the queried and translated result is provided.
   * @param stateObjectType the {@code Class<S>} of the type of the translated result elements
   * @param query the QueryExpression used to execute the query
   * @param andThen the {@code Function<O,R>} used to translate the O outcome to the R result
   * @return {@code Completes<R>}
   * @param <S> the type of the StateObject being queried
   * @param <O> the type of the outcome of the query
   * @param <R> the final result, being a {@code List<S>}
   */
  protected <S,O,R> Completes<R> queryAll(
          final Class<S> stateObjectType,
          final QueryExpression query,
          final Function<O,R> andThen) {

    objectStore.queryAll(
            query,
            queryResultInterest,
            QueryAttempt.with(Cardinality.All, stateObjectType, query, CompletionTranslator.translatorOrNull(andThen, completesEventually())));

    return completes();
  }

  protected <S,O,R> Completes<R> queryObject(
          final Class<S> stateObjectType,
          final QueryExpression query,
          final Function<O,R> andThen) {

    objectStore.queryObject(
            query,
            queryResultInterest,
            QueryAttempt.with(Cardinality.Object, stateObjectType, query, CompletionTranslator.translatorOrNull(andThen, completesEventually())));

    return completes();
  }

  /**
   * @see io.vlingo.xoom.symbio.store.object.ObjectStoreReader.QueryResultInterest#queryAllResultedIn(io.vlingo.xoom.common.Outcome, io.vlingo.xoom.symbio.store.object.ObjectStoreReader.QueryMultiResults, java.lang.Object)
   */
  @Override
  final public void queryAllResultedIn(
          final Outcome<StorageException, Result> outcome,
          final QueryMultiResults queryResults,
          final Object attempt) {
    outcome
      .andThen(result -> {
        completeUsing(attempt, queryResults.stateObjects);
        return result;
      })
      .otherwise(cause -> {
        switch (cause.result) {
        case NotFound:
          completeUsing(attempt, Collections.emptyList());
          return cause.result;
        default:
          break;
        }
        final String message = "Query failed because: " + cause.result + " with: " + cause.getMessage();
        final ObjectQueryFailedException exception = new ObjectQueryFailedException(QueryAttempt.from(attempt), message, cause);
        final Optional<ObjectQueryFailedException> maybeException = afterQueryFailed(exception);
        if (maybeException.isPresent()) {
          logger().error(message, maybeException.get());
          throw maybeException.get();
        }
        logger().error(message, exception);
        return cause.result;
      });
  }

  /*
   * @see io.vlingo.xoom.symbio.store.object.ObjectStoreReader.QueryResultInterest#queryObjectResultedIn(io.vlingo.xoom.common.Outcome, io.vlingo.xoom.symbio.store.object.ObjectStoreReader.QuerySingleResult, java.lang.Object)
   */
  @Override
  final public void queryObjectResultedIn(
          final Outcome<StorageException, Result> outcome,
          final QuerySingleResult queryResult,
          final Object attempt) {
    outcome
      .andThen(result -> {
        completeUsing(attempt, queryResult.stateObject);
        return result;
      })
      .otherwise(cause -> {
        switch (cause.result) {
        case NotFound:
          completeUsing(attempt, queryResult.stateObject);
          return cause.result;
        default:
          break;
        }
        final String message = "Query failed because: " + cause.result + " with: " + cause.getMessage();
        final ObjectQueryFailedException exception = new ObjectQueryFailedException(QueryAttempt.from(attempt), message, cause);
        final Optional<ObjectQueryFailedException> maybeException = afterQueryFailed(exception);
        if (maybeException.isPresent()) {
          logger().error(message, maybeException.get());
          throw maybeException.get();
        }
        logger().error(message, exception);
        return cause.result;
      });
  }

  /**
   * Dispatches to the {@code translator} to complete my protocol
   * message that answers an eventual outcome.
   * @param translator the Object that is cast to a {@code CompletionTranslator<O,?>} and then completed
   * @param outcome the O outcome to be translated for completion
   * @param <O> the type of outcome
   */
  private <O> void completeUsing(final Object attempt, final O outcome) {
    if (attempt != null) {
      QueryAttempt.from(attempt).completionTranslator.complete(outcome);
    }
  }
}
