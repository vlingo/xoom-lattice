// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import io.vlingo.actors.Actor;
import io.vlingo.common.Outcome;
import io.vlingo.common.Tuple3;
import io.vlingo.lattice.model.CompletionSupplier;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

import java.util.List;
import java.util.function.Supplier;

/**
 * Abstract base for all entity types that require the id-clob/blob state storage
 * typical of a CQRS Command Model and CQRS Query Model. Therefore, extend me
 * for both your Command Model and CQRS Query Model, or for your CQRS Query Model
 * only when your Command Model uses the {@code EventSourced} or {@code EventSourced}.
 */
public abstract class StatefulEntity<S> extends Actor
    implements ReadResultInterest, WriteResultInterest {

  private int currentVersion;
  private final Info<S> info;
  private final ReadResultInterest readInterest;
  private final WriteResultInterest writeInterest;

  /**
   * Construct my default state.
   */
  protected StatefulEntity() {
    this.currentVersion = 0;
    this.info = stage().world().resolveDynamic(StatefulTypeRegistry.INTERNAL_NAME, StatefulTypeRegistry.class).info(stateType());
    this.readInterest = selfAs(ReadResultInterest.class);
    this.writeInterest = selfAs(WriteResultInterest.class);
  }

  /*
   * @see io.vlingo.actors.Actor#start()
   */
  @Override
  public void start() {
    super.start();

    final Tuple3<S,List<Source<String>>,String> newState = whenNewState();

    if (newState == null) {
      restore(true); // ignore not found (possible first time start)
    } else {
      apply(newState._1, newState._2, "", newState._3, null);
    }
  }

  /**
   * Answer my currentVersion, which, if zero, indicates that the
   * receiver is being initially constructed or reconstituted.
   * @return int
   */
  protected int currentVersion() {
    return currentVersion;
  }

  /**
   * Answer my unique identity, which much be provided by
   * my concrete extender by overriding.
   * @return String
   */
  protected abstract String id();

  /**
   * Apply my current {@code state} and {@code metadataValye} that was modified
   * due to the descriptive {@code operation}, along with {@code sources}, and
   * supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the S typed state to apply
   * @param sources the {@code List<Source>} instances to apply
   * @param metadataValue the String metadata value to apply along with the state
   * @param operation the String descriptive name of the operation that caused the state modification
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <C,RT> void apply(final S state, final List<Source<C>> sources, final String metadataValue, final String operation, final Supplier<RT> andThen) {
    final Metadata metadata = Metadata.with(state, metadataValue == null ? "" : metadataValue, operation == null ? "" : operation);
    stowMessages(WriteResultInterest.class);
    info.store.write(id(), state, nextVersion(), sources, metadata, writeInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Apply my current {@code state} and {@code metadataValye} that was modified
   * due to the descriptive {@code operation} and supply an eventual outcome by means
   * of the given {@code andThen} function.
   * @param state the S typed state to apply
   * @param metadataValue the String metadata value to apply along with the state
   * @param operation the String descriptive name of the operation that caused the state modification
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <RT> void apply(final S state, final String metadataValue, final String operation, final Supplier<RT> andThen) {
    final Metadata metadata = Metadata.with(state, metadataValue == null ? "" : metadataValue, operation == null ? "" : operation);
    stowMessages(WriteResultInterest.class);
    info.store.write(id(), state, nextVersion(), metadata, writeInterest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Apply my current {@code state} that was modified due to the descriptive {@code operation}
   * and supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the S typed state to apply
   * @param sources the {@code List<Source>} instances to apply
   * @param operation the String descriptive name of the operation that caused the state modification
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <C,RT> void apply(final S state, final List<Source<C>> sources, final String operation, final Supplier<RT> andThen) {
    apply(state, "", operation, andThen);
  }

  /**
   * Apply my current {@code state} that was modified due to the descriptive {@code operation}
   * and supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the S typed state to apply
   * @param operation the String descriptive name of the operation that caused the state modification
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <RT> void apply(final S state, final String operation, final Supplier<RT> andThen) {
    apply(state, "", operation, andThen);
  }

  /**
   * Apply my current {@code state} and supply an eventual outcome by means of the given
   * {@code andThen} function.
   * @param state the S typed state to apply
   * @param sources the {@code List<Source>} instances to apply
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <C> the type of Source
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <C,RT> void apply(final S state, final List<Source<C>> sources, final Supplier<RT> andThen) {
    apply(state, sources, "", "", andThen);
  }

  /**
   * Apply my current {@code state} and supply an eventual outcome by means of the given
   * {@code andThen} function.
   * @param state the S typed state to apply
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <RT> void apply(final S state, final Supplier<RT> andThen) {
    apply(state, "", "", andThen);
  }

  /**
   * Apply my current {@code state} and {@code metadataValye} that was modified
   * due to the descriptive {@code operation}.
   * @param state the S typed state to apply
   * @param sources the {@code List<Source>} instances to apply
   * @param metadataValue the String metadata value to apply along with the state
   * @param operation the String descriptive name of the operation that caused the state modification
   * @param <C> the type of Source
   */
  protected <C> void apply(final S state, final List<Source<C>> sources, final String metadataValue, final String operation) {
    apply(state, sources, metadataValue, operation, null);
  }

  /**
   * Apply my current {@code state} and {@code metadataValye} that was modified
   * due to the descriptive {@code operation}.
   * @param state the S typed state to apply
   * @param metadataValue the String metadata value to apply along with the state
   * @param operation the String descriptive name of the operation that caused the state modification
   */
  protected void apply(final S state, final String metadataValue, final String operation) {
    apply(state, metadataValue, operation, null);
  }

  /**
   * Apply my current {@code state} that was modified due to the descriptive {@code operation}.
   * @param state the S typed state to apply
   * @param sources the {@code List<Source>} instances to apply
   * @param operation the String descriptive name of the operation that caused the state modification
   * @param <C> the type of Source
   */
  protected <C> void apply(final S state, final List<Source<C>> sources, final String operation) {
    apply(state, "", operation, null);
  }

  /**
   * Apply my current {@code state} that was modified due to the descriptive {@code operation}.
   * @param state the S typed state to apply
   * @param operation the String descriptive name of the operation that caused the state modification
   */
  protected void apply(final S state, final String operation) {
    apply(state, "", operation, null);
  }

  /**
   * Apply my current {@code state}.
   * @param state the S typed state to apply
   * @param sources the {@code List<Source>} instances to apply
   * @param <C> the type of Source
   */
  protected <C> void apply(final S state, final List<Source<C>> sources) {
    apply(state, sources, "", "", null);
  }

  /**
   * Apply my current {@code state}.
   * @param state the S typed state to apply
   */
  protected void apply(final S state) {
    apply(state, "", "", null);
  }

  /**
   * Answer my new {@code state}, {@code sources}, and {@code operation} as a
   * {@code Tuple3<S,List<Source<C>>,String>}, or {@code null} if not new.
   * Used each time I am started to determine whether restoration is necessary
   * or otherwise initial state persistence. By default I always attempt to
   * restore my state while ignoring non-existence.
   * @param <C> the type of Source
   * @return {@code Tuple3<S,List<Source<C>>,String>}
   */
  protected <C> Tuple3<S,List<Source<C>>,String> whenNewState() {
    return null;
  }

  /**
   * Restore my current state, dispatching to {@code state(final S state)} when completed.
   */
  protected void restore() {
    restore(false);
  }

  /**
   * Received by my extender when my current state has been applied and restored.
   * Must be overridden by my extender.
   * @param state the S typed state
   */
  protected abstract void state(final S state);

  /**
   * Received by my extender when I must know it's state type.
   * Must be overridden by my extender.
   * @return {@code Class<S>}
   */
  protected abstract Class<S> stateType();

  //=====================================
  // FOR INTERNAL USE ONLY.
  //=====================================

  /*
   * @see io.vlingo.symbio.store.state.StateStore.ReadResultInterest#readResultedIn(io.vlingo.common.Outcome, java.lang.String, java.lang.Object, int, io.vlingo.symbio.Metadata, java.lang.Object)
   */
  @Override
  @SuppressWarnings("unchecked")
  final public <ST> void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final ST state, final int stateVersion, final Metadata metadata, final Object object) {
    outcome
      .andThen(result -> {
        state((S) state);
        currentVersion = stateVersion;
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "State not restored for: " + getClass() + "(" + id + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().error(message, cause);
        throw new IllegalStateException(message, cause);
      });
  }

  /*
   * @see io.vlingo.symbio.store.state.StateStore.WriteResultInterest#writeResultedIn(io.vlingo.common.Outcome, java.lang.String, java.lang.Object, int, java.lang.Object)
   */
  @Override
  @SuppressWarnings("unchecked")
  final public <ST,C> void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final ST state, final int stateVersion, final List<Source<C>> sources, final Object supplier) {
    outcome
      .andThen(result -> {
        state((S) state);
        currentVersion = stateVersion;
        completeUsing(supplier);
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "State not applied for: " + getClass() + "(" + id + ") because: " + cause.result + " with: " + cause.getMessage();
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

  /**
   * Answer my {@code nextVersion}, which is one greater than my {@code currentVersion}.
   * @return int
   */
  private int nextVersion() {
    return currentVersion + 1;
  }

  /**
   * Cause state restoration and indicate whether a not found
   * condition can be safely ignored.
   * @param ignoreNotFound the boolean indicating whether or not a not found condition may be ignored
   */
  private void restore(final boolean ignoreNotFound) {
    stowMessages(ReadResultInterest.class);
    info.store.read(id(), info.storeType, readInterest);
  }
}
