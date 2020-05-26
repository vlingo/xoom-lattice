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
import io.vlingo.lattice.CompositeIdentitySupport;
import io.vlingo.lattice.model.DomainEvent;
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
    implements Projection, CompositeIdentitySupport, ReadResultInterest, WriteResultInterest {

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
   * Answer whether to always write or to compare the {@code currentData} with
   * the {@code previousData} and only write if the two are different. The answer
   * is {@code true} by default, meaning that the write will always happen, even
   * if the {@code currentData} isn't different from the {@code previousData}.
   * Override to answer {@code false} to cause a comparison to qualify the write.
   * @return boolean
   */
  protected boolean alwaysWrite() {
    return true;
  }

  /**
   * Answer the {@code T} typed current data from the {@code projectable}.
   * @param projectable the Projectable from which the current data is retrieved
   * @return T
   */
  protected abstract T currentDataFor(final Projectable projectable);

  /**
   * Answer the current data version. By default this method answers in one of two
   * conditional ways: (1) when my {@code alwaysWrite()} answers {@code true} then
   * the answer is the {@code projectable.dataVersion()} of the received
   * {@code Projectable}; or (2) when my {@code alwaysWrite()} answers {@code false}
   * the {@code previousVersion + 1}. Override for specialized behavior.
   * @param projectable the Projectable containing state and/or entries to be projected
   * @param previousData the T typed previous data from storage
   * @param previousVersion the int previous version from storage
   * @return int
   */
  protected int currentDataVersionFor(final Projectable projectable, final T previousData, final int previousVersion) {
    return alwaysWrite() ? projectable.dataVersion() : (previousVersion == -1 ? 1 : (previousVersion + 1));
  }

  /**
   * Answer the id to be associated with the data being projected.
   * @param projectable the Projectable from which the data id is retrieved
   * @return String
   */
  protected String dataIdFor(final Projectable projectable) {
    return projectable.dataId();
  }

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
   * Prepare for the merge.
   * @param projectable the Projectable used for merge preparation
   */
  protected void prepareForMergeWith(final Projectable projectable) { }

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
   * Upsert the {@code projectable} into the {@code StateStore}, which may be an insert of
   * new data or an update of new data merged with previous data.
   * @param projectable the Projectable to upsert
   * @param control the ProjectionControl with Confirmer use to confirm projection is completed
   */
  protected void upsertFor(final Projectable projectable, final ProjectionControl control) {
    final T currentData = currentDataFor(projectable);

    prepareForMergeWith(projectable);

    final String dataId = dataIdFor(projectable);

    final BiConsumer<T,Integer> upserter = (previousData, previousVersion) -> {
      final int currentDataVersion = currentDataVersionFor(projectable, previousData, previousVersion);
      final T data = merge(previousData, previousVersion, currentData, currentDataVersion);
      final Confirmer confirmer = ProjectionControl.confirmerFor(projectable, control);
      if (alwaysWrite() || !data.equals(previousData)) {
        stateStore.write(dataId, data, currentDataVersion, writeInterest, confirmer);
      } else {
        confirmProjection(confirmer);
      }
    };

    stowMessages(ReadResultInterest.class, WriteResultInterest.class);

    stateStore.read(dataId, currentData.getClass(), readInterest, upserter);
  }

  /**
   * Answer the S typed state from the abstract {@code state}.
   * @param state the Object to cast to type S
   * @param <S> the concrete type of the state
   * @return S
   */
  @SuppressWarnings("unchecked")
  protected <S> S typed(final Object state) {
    return (S) state;
  }

  /**
   * Answer the E typed {@code DomainEvent} from the abstract {@code event}.
   * @param event the DomainEvent to cast to type E
   * @param <E> the concrete type of the event
   * @return E
   */
  @SuppressWarnings("unchecked")
  protected <E> E typed(final DomainEvent event) {
    return (E) event;
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
  final public <S,C> void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final List<Source<C>> sources, final Object object) {
    outcome.andThen(result -> {
      confirmProjection((Confirmer) object);
      return result;
    }).otherwise(cause -> {
      disperseStowedMessages();
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

  private void confirmProjection(final Confirmer confirmer) {
    confirmer.confirm();
    disperseStowedMessages();
  }
}
