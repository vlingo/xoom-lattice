// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.vlingo.actors.CompletionSupplier;
import io.vlingo.common.Completes;
import io.vlingo.common.Outcome;
import io.vlingo.common.Tuple2;
import io.vlingo.lattice.model.EntityGridActor;
import io.vlingo.lattice.model.object.ObjectTypeRegistry.Info;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.store.ListQueryExpression;
import io.vlingo.symbio.store.MapQueryExpression;
import io.vlingo.symbio.store.QueryExpression;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.object.ObjectStoreReader.QueryMultiResults;
import io.vlingo.symbio.store.object.ObjectStoreReader.QueryResultInterest;
import io.vlingo.symbio.store.object.ObjectStoreReader.QuerySingleResult;
import io.vlingo.symbio.store.object.ObjectStoreWriter.PersistResultInterest;
import io.vlingo.symbio.store.object.StateObject;
import io.vlingo.symbio.store.object.StateSources;

/**
 * Abstract base type used to preserve and restore object state
 * by means of the {@code ObjectStore}. The {@code ObjectStore}
 * is typically backed by some form of object-relational mapping,
 * whether formally or informally implemented.
 * @param <T> the type of persistent object
 */
public abstract class ObjectEntity<T extends StateObject> extends EntityGridActor
  implements PersistResultInterest, QueryResultInterest {

  protected final String id;

  private final Info<T> info;
  private final PersistResultInterest persistResultInterest;
  private final QueryResultInterest queryResultInterest;
  private QueryExpression queryExpression;

  /**
   * Construct my default state using my {@code address} as my {@code id}.
   */
  protected ObjectEntity() {
    this(null);
  }

  /**
   * Construct my default state.
   * @param id the String unique identity of this entity
   */
  protected ObjectEntity(final String id) {
    this.id = id != null ? id : address().idString();
    this.info = info();
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
   * Answer {@code Completes<RT>}, applying {@code state} and {@code sources},
   * dispatching to {@code state(final S state)} when completed, and supply an
   * eventual outcome by means of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param sources the {@code List<Source<C>>} instances to apply
   * @param metadata the Metadata to apply along with source
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   * @return {@code Completes<RT>}
   */
  protected <C,RT> Completes<RT> apply(final T state, final List<Source<C>> sources, final Metadata metadata, final Supplier<RT> andThen) {
    stowMessages(PersistResultInterest.class);
    info.store.persist(StateSources.of(state,sources), metadata, persistResultInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
    return andThen == null ? null : completes();
  }

  /**
   * Answer {@code Completes<RT>}, applying {@code state} and {@code sources},
   * dispatching to {@code state(final S state)} when completed, and supply an
   * eventual outcome by means of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param sources the {@code List<Source<C>>} instances to apply
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   * @return {@code Completes<RT>}
   */
  protected <C,RT> Completes<RT> apply(final T state, final List<Source<C>> sources, final Supplier<RT> andThen) {
    return apply(state, sources, metadata(), andThen);
  }

  /**
   * Answer {@code Completes<RT>}, applying {@code state} and {@code source}, dispatching to
   * {@code state(final S state)} when completed and supply an eventual outcome by means of
   * the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param source the {@code Source<C>} to apply
   * @param metadata the Metadata to apply along with source
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   * @return {@code Completes<RT>}
   */
  protected <C,RT> Completes<RT> apply(final T state, final Source<C> source, final Metadata metadata, final Supplier<RT> andThen) {
    stowMessages(PersistResultInterest.class);
    info.store.persist(StateSources.of(state, source), metadata, persistResultInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
    return andThen == null ? null : completes();
  }

  /**
   * Answer {@code Completes<RT>}, applying {@code state} and {@code source}, dispatching to
   * {@code state(final S state)} when completed and supply an eventual outcome by means of
   * the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param source the {@code Source<C>} to apply
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   * @return {@code Completes<RT>}
   */
  protected <C,RT> Completes<RT> apply(final T state, final Source<C> source, final Supplier<RT> andThen) {
    return apply(state, source, metadata(), andThen);
  }

  /**
   * Answer {@code Completes<RT>}, applying my current state, dispatching to
   * {@code state(final S state)} when completed, and supply an eventual outcome by means
   * of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   * @return {@code Completes<RT>}
   */
  protected <RT> Completes<RT> apply(final T state, final Supplier<RT> andThen) {
    stowMessages(PersistResultInterest.class);
    info.store.persist(StateSources.of(state), persistResultInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
    return andThen == null ? null : completes();
  }

  /**
   * Apply my current {@code state} and {@code sources}.
   * @param state the T typed state to apply
   * @param sources the {@code List<Source<C>>} instances to apply
   * @param <C> the type of Source
   */
  protected <C> void apply(final T state, final List<Source<C>> sources) {
    apply(state, sources, metadata(), null);
  }

  /**
   * Apply my current {@code state} and {@code source}.
   * @param state the T typed state to apply
   * @param source the {@code Source<C>} instances to apply
   * @param <C> the type of Source
   */
  protected <C> void apply(final T state, final Source<C> source) {
    apply(state, Arrays.asList(source), metadata(), null);
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
   * Answer a representation of a number of segments as a
   * composite id. The implementor of {@code id()} would use
   * this method if the its id is built from segments.
   * @param separator the String separator the insert between segments
   * @param idSegments the varargs String of one or more segments
   * @return String
   */
  protected String idFrom(final String separator, final String... idSegments) {
    final StringBuilder builder = new StringBuilder();
    builder.append(idSegments[0]);
    for (int idx = 1; idx < idSegments.length; ++idx) {
      builder.append(separator).append(idSegments[idx]);
    }
    return builder.toString();
  }

  /**
   * Answer my {@code Metadata}. Must override if {@code Metadata} is to be supported.
   * @return Metadata
   */
  protected Metadata metadata() {
    return Metadata.nullMetadata();
  }

  /**
   * Received by my extender when I must access its state object.
   * Must be overridden by my extender.
   * @return T
   */
  protected abstract T stateObject();

  /**
   * Received by my extender when my state object has been preserved and restored.
   * Must be overridden by my extender.
   * @param stateObject the T typed state object
   */
  protected abstract void stateObject(final T stateObject);

  /**
   * Received by my extender when I must know it's state object type.
   * Must be overridden by my extender.
   * @return {@code Class<S>}
   */
  protected abstract Class<T> stateObjectType();

  /**
   * Restore my current state, dispatching to {@code state(final S state)} when completed.
   */
  @Override
  protected final void restore() {
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
        stateObject((T) queryResult.stateObject);
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        disperseStowedMessages();
        final boolean ignoreNotFound = (boolean) object;
        if (!ignoreNotFound) {
          final String message = "State not restored for: " + getClass() + "(" + this.id + ") because: " + cause.result + " with: " + cause.getMessage();
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
      stateObject((T) persistentObject);
      afterApply();
      completeUsing(supplier);
      disperseStowedMessages();
      return result;
    })
    .otherwise(cause -> {
      disperseStowedMessages();
      final String message = "State not preserved for: " + getClass() + "(" + this.id + ") because: " + cause.result + " with: " + cause.getMessage();
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

  private Info<T> info() {
    try {
      final ObjectTypeRegistry registry = stage().world().resolveDynamic(ObjectTypeRegistry.INTERNAL_NAME, ObjectTypeRegistry.class);
      final Info<T> info = registry.info(stateObjectType());
      return info;
    } catch (Exception e) {
      final String message = getClass().getSimpleName() + ": Info not registered with ObjectTypeRegistry.";
      logger().error(message);
      throw new IllegalStateException(message);
    }
  }

  private QueryExpression queryExpression() {
    if (queryExpression == null) {
      if (info.queryObjectExpression.isListQueryExpression()) {
        queryExpression =
                ListQueryExpression.using(
                        info.queryObjectExpression.type,
                        info.queryObjectExpression.query,
                        stateObject().queryList());
      } else if (info.queryObjectExpression.isMapQueryExpression()) {
        queryExpression =
                MapQueryExpression.using(
                        info.queryObjectExpression.type,
                        info.queryObjectExpression.query,
                        stateObject().queryMap());
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
