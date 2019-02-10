// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import java.util.Arrays;
import java.util.function.Supplier;

import io.vlingo.actors.Actor;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.CompletionSupplier;
import io.vlingo.lattice.model.object.ObjectEntityTypeRegistry.Info;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.object.ListQueryExpression;
import io.vlingo.symbio.store.object.MapQueryExpression;
import io.vlingo.symbio.store.object.ObjectStore.PersistResultInterest;
import io.vlingo.symbio.store.object.ObjectStore.QueryMultiResults;
import io.vlingo.symbio.store.object.ObjectStore.QueryResultInterest;
import io.vlingo.symbio.store.object.ObjectStore.QuerySingleResult;
import io.vlingo.symbio.store.object.QueryExpression;

/**
 * Abstract base type used to preserve and restore object state
 * by means of the {@code ObjectStore}. The {@code ObjectStore}
 * is typically backed by some form of object-relational mapping,
 * whether formally or informally implemented.
 */
public abstract class ObjectEntity<T> extends Actor 
  implements PersistResultInterest, QueryResultInterest {

  private final Info<T> info;
  private final PersistResultInterest persistResultInterest;
  private final QueryResultInterest queryResultInterest;
  private QueryExpression queryExpression;

  /**
   * Construct my default state.
   */
  protected ObjectEntity() {
    this.info = stage().world().resolveDynamic(ObjectEntityTypeRegistry.INTERNAL_NAME, ObjectEntityTypeRegistry.class).info(persistentObjectType());
    this.persistResultInterest = selfAs(PersistResultInterest.class);
    this.queryResultInterest = selfAs(QueryResultInterest.class);
  }

  /*
   * @see io.vlingo.actors.Actor#start()
   */
  @Override
  public void start() {
    super.start();

    restore(true); // ignore not found (possible first time start)
  }

  /**
   * Answer my unique identity, which much be provided by
   * my concrete extender by overriding.
   * @return String
   */
  protected abstract String id();

  /**
   * Received by my extender when my persistent object has been preserved and restored.
   * Must be overridden by my extender.
   * @param persistentObject the T typed persistent object
   */
  protected abstract void persistentObject(final T persistentObject);

  /**
   * Received by my extender when I must know it's persistent object type.
   * Must be overridden by my extender.
   * @return {@code Class<S>}
   */
  protected abstract Class<T> persistentObjectType();

  /**
   * Preserve my current state dispatching to {@code state(final S state)} when completed
   * and supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <RT> void preserve(final Object state, final Supplier<RT> andThen) {
    stowMessages(PersistResultInterest.class);
    info.store.persist(state, persistResultInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Restore my current state, dispatching to {@code state(final S state)} when completed.
   */
  protected void restore() {
    restore(false);
  }

  //=====================================
  // FOR INTERNAL USE ONLY.
  //=====================================

  /*
   * @see io.vlingo.symbio.store.object.ObjectStore.QueryResultInterest#queryAllResultedIn(io.vlingo.common.Outcome, io.vlingo.symbio.store.object.ObjectStore.QueryMultiResults, java.lang.Object)
   */
  @Override
  final public void queryAllResultedIn(final Outcome<StorageException, Result> outcome, final QueryMultiResults results, final Object object) {
    throw new UnsupportedOperationException("Must be unreachable: queryAllResultedIn()");
  }

  /*
   * @see io.vlingo.symbio.store.object.ObjectStore.QueryResultInterest#queryObjectResultedIn(io.vlingo.common.Outcome, io.vlingo.symbio.store.object.ObjectStore.QuerySingleResult, java.lang.Object)
   */
  @Override
  @SuppressWarnings("unchecked")
  final public void queryObjectResultedIn(
          final Outcome<StorageException, Result> outcome,
          final QuerySingleResult queryResult,
          final Object object) {
    outcome
      .andThen(result -> {
        persistentObject((T) queryResult.persistentObject);
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final boolean ignoreNotFound = (boolean) object;
        if (!ignoreNotFound) {
          final String message = "State not restored for: " + getClass() + "(" + id() + ") because: " + cause.result + " with: " + cause.getMessage();
          logger().log(message, cause);
          throw new IllegalStateException(message, cause);
        }
        return cause.result;
      });
  }

  /*
   * @see io.vlingo.symbio.store.object.ObjectStore.PersistResultInterest#persistResultedIn(io.vlingo.common.Outcome, java.lang.Object, int, int, java.lang.Object)
   */
  @Override
  @SuppressWarnings("unchecked")
  final public void persistResultedIn(
          final Outcome<StorageException, Result> outcome,
          final Object persistentObject,
          final int possible,
          final int actual,
          final Object supplier) {
    outcome
    .andThen(result -> {
      persistentObject((T) persistentObject);
      completeUsing(supplier);
      disperseStowedMessages();
      return result;
    })
    .otherwise(cause -> {
      final String message = "State not preserved for: " + getClass() + "(" + id() + ") because: " + cause.result + " with: " + cause.getMessage();
      logger().log(message, cause);
      throw new IllegalStateException(message, cause);
    });
  }

  /**
   * Dispatches to the {@code supplier} to complete my protocol
   * message that answers an eventual outcome.
   * @param supplier the Object that is cast to a {@code CompletionSupplier<?>} and then completed
   */
  private void completeUsing(final Object supplier) {
    if (supplier != null) {
      ((CompletionSupplier<?>) supplier).complete();
    }
  }

  private QueryExpression queryExpression() {
    if (queryExpression == null) {
      if (info.queryObjectExpression.isListQueryExpression()) {
        queryExpression =
                ListQueryExpression.using(
                        info.queryObjectExpression.type,
                        info.queryObjectExpression.query,
                        Arrays.asList(id()));
      } else if (info.queryObjectExpression.isMapQueryExpression()) {
        queryExpression =
                MapQueryExpression.using(
                        info.queryObjectExpression.type,
                        info.queryObjectExpression.query,
                        MapQueryExpression.map("id", id()));
      } else {
        throw new IllegalStateException("Unknown QueryExpression type: " + queryExpression);
      }
    }
    return queryExpression;
  }

  /**
   * Cause state restoration and indicate whether a not found
   * condition can be safely ignored.
   * @param ignoreNotFound the boolean indicating whether or not a not found condition may be ignored
   */
  private void restore(final boolean ignoreNotFound) {
    stowMessages(QueryResultInterest.class);
    info.store.queryObject(queryExpression(), queryResultInterest, ignoreNotFound);
  }
}
