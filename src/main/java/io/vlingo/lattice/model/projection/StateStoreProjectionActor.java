// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.List;
import java.util.function.BiConsumer;

import io.vlingo.actors.Actor;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.projection.ProjectionControl.Confirmer;
import io.vlingo.symbio.DefaultTextEntryAdapter;
import io.vlingo.symbio.DefaultTextStateAdapter;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.EntryAdapter;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.State;
import io.vlingo.symbio.StateAdapter;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

/**
 * Abstract {@code Actor} base class used by {@code Projection} types to handle
 * {@code projectWith()} into the {@code StateStore}. Concrete extenders must:
 *
 * <p>- Provide the {@code StateStore} for construction
 * <p>- Implement {@code StateStoreProjectionActor#currentDataFor(Projectable)}
 * <p>- Implement {@code StateStoreProjectionActor#merge(T, int, T, int)}
 * <p>- Invoke {@code StateStoreProjectionActor#upsertFor(Projectable, ProjectionControl)} to cause the upsert
 *
 * @param <T> the type to be persisted in the StateStore
 */
public abstract class StateStoreProjectionActor<T> extends Actor
    implements Projection, ReadResultInterest, WriteResultInterest {

  private final EntryAdapter<Source<?>, Entry<?>> entryAdapter;
  private final StateAdapter<Object, State<?>> stateAdapter;
  private final ReadResultInterest readInterest;
  private final WriteResultInterest writeInterest;
  private final StateStore stateStore;

  /**
   * Construct my final state with the {@code StateStore}, which must
   * be provided by my concrete extenders. I provide default state and
   * entry adapters.
   * @param stateStore the StateStore from which previous state is read and merged current state is written
   */
  public StateStoreProjectionActor(final StateStore stateStore) {
    this(stateStore, defaultTextStateAdapter(), defaultTextEntryAdapter());
  }

  /**
   * Construct my final state with the {@code StateStore}, which must
   * be provided by my concrete extenders, as well as with a
   * {@code stateAdapter} and a {@code entryAdapter}.
   * @param stateStore the StateStore from which previous state is read and merged current state is written
   * @param stateAdapter the {@code StateAdapter<Object, State<?>>} used by my extenders to adapt persistent state
   * @param entryAdapter the {@code EntryAdapter<Source<?>, Entry<?>>} used by my extenders to adapt persistent entries
   */
  public StateStoreProjectionActor(
          final StateStore stateStore,
          final StateAdapter<Object, State<?>> stateAdapter,
          final EntryAdapter<Source<?>, Entry<?>> entryAdapter) {

    this.stateStore = stateStore;
    this.stateAdapter = stateAdapter;
    this.entryAdapter = entryAdapter;
    this.readInterest = selfAs(ReadResultInterest.class);
    this.writeInterest = selfAs(WriteResultInterest.class);
  }

  /**
   * @see io.vlingo.lattice.model.projection.Projection#projectWith(io.vlingo.lattice.model.projection.Projectable, io.vlingo.lattice.model.projection.ProjectionControl)
   */
  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    upsertFor(projectable, control);
  }

  /**
   * Answer the {@code T} typed current data from the {@code projectable}.
   * @param projectable the Projectable from which the current data is retrieved
   * @return T
   */
  protected abstract T currentDataFor(final Projectable projectable);

  /**
   * Answer the {@code EntryAdapter<S,E>} previously registered by construction.
   * @param <S> the {@code Source<?>} type
   * @param <E> the {@code Entry<?>} type
   * @return {@code EntryAdapter<S,E>}
   */
  @SuppressWarnings("unchecked")
  protected <S extends Source<?>, E extends Entry<?>> EntryAdapter<S,E> entryAdapter() {
    return (EntryAdapter<S,E>) this.entryAdapter;
  }

  /**
   * Answer the {@code T} result of merging the {@code T}-typed {@code previousData} and {@code currentData},
   * which will be written into the {@code StateStore}. This method will not be invoked if the previous data
   * of the projection is not found in the {@code StateStore}. The receiver may simple answer the
   * {@code currentData} when no merging is required, resulting in {@code currentData} being written.
   * @param previousData the T data read from the StateStore
   * @param previousVersion the int version of the previousData
   * @param currentData the T data being projected
   * @param currentVersion the int version of the currentData
   * @return T
   */
  protected abstract T merge(final T previousData, final int previousVersion, final T currentData, final int currentVersion);

  /**
   * Answer the {@code StateAdapter<?,ST>} previously registered by construction.
   * @param <ST> the {@code State<?>} type
   * @return {@code StateAdapter<?,ST>}
   */
  @SuppressWarnings("unchecked")
  protected <ST extends State<?>> StateAdapter<?,ST> stateAdapter() {
    return (StateAdapter<?,ST>) this.stateAdapter;
  }

  /**
   * Upsert the {@code projectable} into the {@code StateStore}, which may be a
   * @param projectable the Projectable to upsert
   * @param control the ProjectionControl with Confirmer use to confirm projection is completed
   */
  protected void upsertFor(final Projectable projectable, final ProjectionControl control) {
    final T currentData = currentDataFor(projectable);
    final int currentDataVersion = projectable.dataVersion();

    final BiConsumer<T,Integer> upserter = (previousData, previousVersion) -> {
      final T data = previousData == null ? currentData : merge(previousData, previousVersion, currentData, currentDataVersion);
      stateStore.write(projectable.dataId(), data, currentDataVersion, writeInterest, control.confirmerFor(projectable));
    };

    stateStore.read(projectable.dataId(), currentData.getClass(), readInterest, upserter);
  }

  //==================================
  // ReadResultInterest
  //==================================

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <S> void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final Metadata metadata, final Object object) {
    outcome.andThen(result -> {
      ((BiConsumer<S,Integer>) object).accept(state, stateVersion);
      return result;
    }).otherwise(cause -> {
      if (cause.result.isNotFound()) {
        ((BiConsumer<S,Integer>) object).accept(null, -1);
      } else {
        // log but don't retry, allowing re-delivery of Projectable
        logger().info("Query state not read for update because: " + cause.getMessage(), cause);
      }
      return cause.result;
    });
  }

  //==================================
  // WriteResultInterest
  //==================================

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  public <S,C> void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final List<Source<C>> sources, final Object object) {
    outcome.andThen(result -> {
      ((Confirmer) object).confirm();
      return result;
    }).otherwise(cause -> {
      // log but don't retry, allowing re-delivery of Projectable
      logger().info("Query state not written for update because: " + cause.getMessage(), cause);
      return cause.result;
    });
  }

  //==================================
  // Internal Implementation
  //==================================

  @SuppressWarnings("unchecked")
  private static <S extends Source<?>, E extends Entry<?>> EntryAdapter<S, E> defaultTextEntryAdapter() {
    return new DefaultTextEntryAdapter();
  }

  @SuppressWarnings("unchecked")
  private static <S, ST extends State<?>> StateAdapter<S, ST> defaultTextStateAdapter() {
    return (StateAdapter<S, ST>) new DefaultTextStateAdapter();
  }
}
