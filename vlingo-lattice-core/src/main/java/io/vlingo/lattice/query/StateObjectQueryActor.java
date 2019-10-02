// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.query;

import java.util.function.Function;

import io.vlingo.actors.Actor;
import io.vlingo.common.Completes;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.CompletionTranslator;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.object.ObjectStore;
import io.vlingo.symbio.store.object.ObjectStoreReader.QueryMultiResults;
import io.vlingo.symbio.store.object.ObjectStoreReader.QueryResultInterest;
import io.vlingo.symbio.store.object.ObjectStoreReader.QuerySingleResult;
import io.vlingo.symbio.store.object.QueryExpression;

/**
 * An building-block {@code Actor} that queries asynchronously and provides a translated outcome.
 */
public class StateObjectQueryActor extends Actor implements QueryResultInterest {
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

    objectStore.queryAll(query, queryResultInterest, CompletionTranslator.translatorOrNull(andThen, completesEventually()));

    return completes();
  }

  protected <S,O,R> Completes<R> queryObject(
          final Class<S> stateObjectType,
          final QueryExpression query,
          final Function<O,R> andThen) {

    objectStore.queryObject(query, queryResultInterest, CompletionTranslator.translatorOrNull(andThen, completesEventually()));

    return completes();
  }

  /**
   * @see io.vlingo.symbio.store.object.ObjectStoreReader.QueryResultInterest#queryAllResultedIn(io.vlingo.common.Outcome, io.vlingo.symbio.store.object.ObjectStoreReader.QueryMultiResults, java.lang.Object)
   */
  @Override
  final public void queryAllResultedIn(
          final Outcome<StorageException, Result> outcome,
          final QueryMultiResults queryResults,
          final Object translator) {
    outcome
      .andThen(result -> {
        completeUsing(translator, queryResults.stateObjects);
        return result;
      })
      .otherwise(cause -> {
        final String message = "Query failed because: " + cause.result + " with: " + cause.getMessage();
        logger().error(message, cause);
        throw new IllegalStateException(message, cause);
      });
  }

  /*
   * @see io.vlingo.symbio.store.object.ObjectStoreReader.QueryResultInterest#queryObjectResultedIn(io.vlingo.common.Outcome, io.vlingo.symbio.store.object.ObjectStoreReader.QuerySingleResult, java.lang.Object)
   */
  @Override
  final public void queryObjectResultedIn(
          final Outcome<StorageException, Result> outcome,
          final QuerySingleResult queryResult,
          final Object supplier) {
    outcome
      .andThen(result -> {
        completeUsing(supplier, queryResult.stateObject);
        return result;
      })
      .otherwise(cause -> {
        final String message = "Query failed because: " + cause.result + " with: " + cause.getMessage();
        logger().error(message, cause);
        throw new IllegalStateException(message, cause);
      });
  }

  /**
   * Dispatches to the {@code translator} to complete my protocol
   * message that answers an eventual outcome.
   * @param translator the Object that is cast to a {@code CompletionTranslator<O,?>} and then completed
   * @param outcome the O outcome to be translated for completion
   * @param <O> the type of outcome
   */
  @SuppressWarnings("unchecked")
  private <O> void completeUsing(final Object translator, final O outcome) {
    if (translator != null) {
      ((CompletionTranslator<O,?>) translator).complete(outcome);
    }
  }
}
