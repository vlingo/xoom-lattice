// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.vlingo.actors.Actor;
import io.vlingo.common.Outcome;
import io.vlingo.common.Tuple2;
import io.vlingo.lattice.model.CompletionSupplier;
import io.vlingo.lattice.model.object.ObjectTypeRegistry.Info;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.object.ListQueryExpression;
import io.vlingo.symbio.store.object.MapQueryExpression;
import io.vlingo.symbio.store.object.ObjectStoreReader.QueryMultiResults;
import io.vlingo.symbio.store.object.ObjectStoreReader.QueryResultInterest;
import io.vlingo.symbio.store.object.ObjectStoreReader.QuerySingleResult;
import io.vlingo.symbio.store.object.ObjectStoreWriter.PersistResultInterest;
import io.vlingo.symbio.store.object.PersistentObject;
import io.vlingo.symbio.store.object.QueryExpression;

/**
 * Abstract base type used to preserve and restore object state
 * by means of the {@code ObjectStore}. The {@code ObjectStore}
 * is typically backed by some form of object-relational mapping,
 * whether formally or informally implemented.
 * @param <T> the type of persistent object
 */
public abstract class ObjectEntity<T extends PersistentObject> extends Actor
  implements PersistResultInterest, QueryResultInterest {

  private final Info<T> info;
  private final PersistResultInterest persistResultInterest;
  private final QueryResultInterest queryResultInterest;
  private QueryExpression queryExpression;

  /**
   * Construct my default state.
   */
  protected ObjectEntity() {
    this.info = stage().world().resolveDynamic(ObjectTypeRegistry.INTERNAL_NAME, ObjectTypeRegistry.class).info(persistentObjectType());
    this.persistResultInterest = selfAs(PersistResultInterest.class);
    this.queryResultInterest = selfAs(QueryResultInterest.class);
  }

  /*
   * @see io.vlingo.actors.Actor#start()
   */
  @Override
  public void start() {
    super.start();

    final Tuple2<T,List<Source<String>>> newState = whenNewState();

    if (newState == null) {
      restore(true); // ignore not found (possible first time start)
    } else {
      apply(newState._1, newState._2, null);
    }
  }

  /**
   * Apply {@code state} and {@code sources}, dispatching to {@code state(final S state)} when completed
   * and supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param sources the {@code List<Source<C>>} instances to apply
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <C,RT> void apply(final T state, final List<Source<C>> sources, final Supplier<RT> andThen) {
    stowMessages(PersistResultInterest.class);
    info.store.persist(state, sources, persistResultInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Apply {@code state} and {@code source}, dispatching to {@code state(final S state)} when completed
   * and supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param source the {@code Source<C>} to apply
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <C,RT> void apply(final T state, final Source<C> source, final Supplier<RT> andThen) {
    stowMessages(PersistResultInterest.class);
    info.store.persist(state, asList(source), persistResultInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Preserve my current state dispatching to {@code state(final S state)} when completed
   * and supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <RT> void apply(final T state, final Supplier<RT> andThen) {
    stowMessages(PersistResultInterest.class);
    info.store.persist(state, persistResultInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Apply my current {@code state} and {@code sources}.
   * @param state the T typed state to apply
   * @param sources the {@code List<Source<C>>} instances to apply
   * @param <C> the type of Source
   */
  protected <C> void apply(final T state, final List<Source<C>> sources) {
    apply(state, sources, null);
  }

  /**
   * Apply my current {@code state} and {@code source}.
   * @param state the T typed state to apply
   * @param source the {@code Source<C>} instances to apply
   * @param <C> the type of Source
   */
  protected <C> void apply(final T state, final Source<C> source) {
    apply(state, Arrays.asList(source), null);
  }

  /**
   * Received after the full asynchronous evaluation of each {@code apply()}.
   * Override if notification is desired.
   */
  protected void afterApply() { }

  /**
   * Answer a {@code List<Source<C>>} from the varargs {@code sources}.
   * @param sources the varargs {@code Source<C>} of sources to answer as a {@code List<Source<C>>}
   * @param <C> the type of Source
   * @return {@code List<Source<C>>}
   */
  @SafeVarargs
  protected final <C> List<Source<C>> asList(final Source<C>... sources) {
    return Arrays.asList(sources);
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
   * Restore my current state, dispatching to {@code state(final S state)} when completed.
   */
  protected void restore() {
    restore(false);
  }

  /**
   * Answer my new {@code state} and {@code sources} as a {@code Tuple2<S,List<Source<C>>},
   * or {@code null} if not new. Used each time I am started to determine
   * whether restoration is necessary or otherwise initial state persistence.
   * By default I always attempt to restore my state while ignoring non-existence.
   * @param <C> the type of Source
   * @return {@code Tuple2<T,List<Source<C>>}
   */
  protected <C> Tuple2<T,List<Source<C>>> whenNewState() {
    return null;
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
          logger().error(message, cause);
          throw new IllegalStateException(message, cause);
        }
        return cause.result;
      });
  }

  /* @see io.vlingo.symbio.store.object.ObjectStoreWriter.PersistResultInterest#persistResultedIn(io.vlingo.common.Outcome, io.vlingo.symbio.store.object.PersistentObject, int, int, java.lang.Object) */
  @Override
  @SuppressWarnings("unchecked")
  public void persistResultedIn(final Outcome<StorageException, Result> outcome, final Object persistentObject, final int possible, final int actual, final Object supplier) {
    outcome
    .andThen(result -> {
      persistentObject((T) persistentObject);
      afterApply();
      completeUsing(supplier);
      disperseStowedMessages();
      return result;
    })
    .otherwise(cause -> {
      final String message = "State not preserved for: " + getClass() + "(" + id() + ") because: " + cause.result + " with: " + cause.getMessage();
      logger().error(message, cause);
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
