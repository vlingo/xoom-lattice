// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestContext;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.CompletionSupplier;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.Journal.AppendResultInterest;

/**
 * Abstract base for all concrete types that support journaling and application of
 * {@code Source<T>} instances. Provides abstracted {@code Journal} and and state
 * transition control for my concrete extender.
 * @param <T> the concrete type that is being sourced
 */
public abstract class Sourced<T> extends Actor implements AppendResultInterest {
  private static final Map<Class<Sourced<Source<?>>>,Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>>> registeredConsumers =
          new ConcurrentHashMap<>();

  private TestContext testContext;
  private int currentVersion;
  private SourcedTypeRegistry.Info<?> journalInfo;
  private AppendResultInterest interest;

  /**
   * Register the means to apply {@code sourceType} instances for state transition
   * of {@code sourcedType} by means of a given {@code consumer}.
   * @param sourcedType the concrete {@code Class<? extends Sourced<?>>} type to which sourceType instances are applied
   * @param sourceType the concrete {@code Class<? extends Source<?>>} type to apply
   * @param consumer the {@code BiConsumer<? extends Sourced<?>, ? extends Source<?>>} used to perform the application of sourceType
   */
  @SuppressWarnings("unchecked")
  public static void registerConsumer(
          Class<? extends Sourced<?>> sourcedType,
          Class<? extends Source<?>> sourceType,
          BiConsumer<? extends Sourced<?>, ? extends Source<?>> consumer) {

    Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(sourcedType);

    if (sourcedTypeMap == null) {
      sourcedTypeMap = new ConcurrentHashMap<>();
      registeredConsumers.put((Class<Sourced<Source<?>>>) sourcedType, sourcedTypeMap);
    }

    sourcedTypeMap.put((Class<Source<?>>) sourceType, (BiConsumer<Sourced<?>, Source<?>>) consumer);
  }

  /*
   * @see io.vlingo.actors.Actor#start()
   */
  @Override
  public void start() {
    super.start();

    restore();
  }

  /*
   * @see io.vlingo.actors.Actor#viewTestStateInitialization(io.vlingo.actors.testkit.TestContext)
   */
  @Override
  public void viewTestStateInitialization(final TestContext context) {
    if (context != null) {
      testContext = context;
      testContext.reference.set(new CopyOnWriteArrayList<>());
    }
  }

  /*
   * @see io.vlingo.actors.Actor#viewTestState()
   */
  @Override
  public TestState viewTestState() {
    final TestState testState = new TestState();
    testState.putValue("applied", testContext.reference.get());
    return testState;
  }

  /**
   * Construct my default state.
   */
  protected Sourced() {
    this.currentVersion = 0;
    this.journalInfo = stage().world().resolveDynamic(SourcedTypeRegistry.INTERNAL_NAME, SourcedTypeRegistry.class).info(getClass());
    this.interest = selfAs(AppendResultInterest.class);
  }

  /**
   * Apply all of the given {@code sources} to myself, which includes appending
   * them to my journal and reflecting the representative changes to my state.
   * @param sources the {@code List<Source<T>>} to apply
   */
  final protected void apply(final List<Source<T>> sources) {
    apply(sources, null);
  }

  /**
   * Apply all of the given {@code sources} to myself, which includes appending
   * them to my journal and reflecting the representative changes to my state,
   * followed by the execution of a possible {@code andThen}.
   * @param sources the {@code List<Source<T>>} to apply
   * @param andThen the {@code Supplier<R>} executed following the application of sources
   * @param <R> the return type of the andThen {@code Supplier<R>}
   */
  final protected <R> void apply(final List<Source<T>> sources, final Supplier<R> andThen) {
    final List<Source<T>> toApply = wrap(sources);
    beforeApply(toApply);
    final Journal<?> journal = journalInfo.journal();
    stowMessages(AppendResultInterest.class);
    journal.appendAllWith(streamName(), nextVersion(), toApply, snapshot(), interest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Apply the given {@code source} to myself, which includes appending it
   * to my journal and reflecting the representative changes to my state.
   * @param source the {@code Source<T>} to apply
   */
  final protected void apply(final Source<T> source) {
    apply(source, null);
  }

  /**
   * Apply the given {@code source} to myself, which includes appending it
   * to my journal and reflecting the representative changes to my state,
   * followed by the execution of a possible {@code andThen}.
   * @param source the {@code Source<T>} to apply
   * @param andThen the {@code Supplier<R>} executed following the application of sources
   * @param <R> the return type of the andThen {@code Supplier<R>}
   */
  final protected <R> void apply(final Source<T> source, final Supplier<R> andThen) {
    final List<Source<T>> toApply = wrap(source);
    beforeApply(toApply);
    final Journal<?> journal = journalInfo.journal();
    stowMessages(AppendResultInterest.class);
    journal.appendAllWith(streamName(), nextVersion(), toApply, snapshot(), interest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
  }

  /**
   * Answer a {@code List<Source<T>>} from the varargs {@code sources}.
   * @param sources the varargs {@code Source<T>} of sources to answer as a {@code List<Source<T>>}
   * @return {@code List<Source<T>>}
   */
  @SuppressWarnings("unchecked")
  protected List<Source<T>> asList(final Source<T>... sources) {
    return Arrays.asList(sources);
  }

  /**
   * Received prior to the evaluation of each {@code apply()} and by
   * default adds all applied {@code Source<T>} {@code sources} to the
   * {@code TestContext reference}, if currently testing. The concrete
   * extender may override to implement different or additional behavior.
   * @param sources the {@code List<Source<T>>} ready to be applied
   */
  protected void beforeApply(final List<Source<T>> sources) {
    // override to be informed prior to apply evaluation
    if (testContext != null) {
      final List<Source<T>> all = testContext.reference();
      all.addAll(sources);
      testContext.until.happened();
    }
  }

  /**
   * Answer my {@code currentVersion}, which if zero indicates that the receiver
   * is being initially constructed or reconstituted.
   * @return int
   */
  protected int currentVersion() {
    return currentVersion;
  }

  /**
   * Answer my next version, which if one greater then my {@code currentVersion}.
   * @return int
   */
  protected int nextVersion() {
    return currentVersion + 1;
  }

  /**
   * Restores the initial state of the receiver by means of the {@code snapshot}.
   * Must override if snapshots are to be supported.
   * @param snapshot the {@code SNAPSHOT} holding the {@code Sourced<T,ST>} initial state
   * @param currentVersion the int current version of the receiver; may be helpful in determining
   * @param <SNAPSHOT> the type of the snapshot 
   */
  protected <SNAPSHOT> void restoreSnapshot(final SNAPSHOT snapshot, final int currentVersion) {
    // OVERIDE FOR SNAPSHOT SUPPORT
  }

  /**
   * Answer a valid {@code SNAPSHOT} state instance if a snapshot should
   * be taken and persisted along with applied {@code Source<T>} instance(s).
   * Must override if snapshots are to be supported.
   * @param <SNAPSHOT> the type of the snapshot 
   * @return {@code SNAPSHOT}
   */
  protected <SNAPSHOT> SNAPSHOT snapshot() {
    return null;
  }

  /**
   * Answer my stream name. Must override.
   * @return String
   */
  protected abstract String streamName();

  /**
   * Answer a representation of a number of segments as a
   * composite stream name. The implementor of {@code streamName()}
   * would use this method if the its stream name is built from segments.
   * @param separator the String separator the insert between segments
   * @param streamNameSegments the varargs String of one or more segments
   * @return String
   */
  protected String streamNameFrom(final String separator, final String... streamNameSegments) {
    final StringBuilder builder = new StringBuilder();
    builder.append(streamNameSegments[0]);
    for (int idx = 1; idx < streamNameSegments.length; ++idx) {
      builder.append(separator).append(streamNameSegments[idx]);
    }
    return builder.toString();
  }

  //==================================
  // AppendResultInterest
  //==================================

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  final public <STT,ST> void appendResultedIn(
          final Outcome<StorageException, Result> outcome,
          final String streamName,
          final int streamVersion,
          final Source<STT> source,
          final Optional<ST> snapshot,
          final Object supplier) {

    outcome
      .andThen(result -> {
        restoreSnapshot(snapshot, currentVersion);
        applyResultVersioned(source);
        completeUsing(supplier);
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        disperseStowedMessages();
        final String message = "Source (count 1) not appended for: " + type() + "(" + streamName() + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().log(message, cause);
        throw new StorageException(cause.result, message, cause);
      });
  }

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  final public <STT,ST> void appendAllResultedIn(
          final Outcome<StorageException, Result> outcome,
          final String streamName,
          final int streamVersion,
          final List<Source<STT>> sources,
          final Optional<ST> snapshot,
          final Object supplier) {

    outcome
      .andThen(result -> {
        restoreSnapshot(snapshot, currentVersion);
        for (final Source<STT> source : sources) {
          applyResultVersioned(source);
        }
        completeUsing(supplier);
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "Source (count " + sources.size() + ") not appended for: " + type() + "(" + streamName() + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().log(message, cause);
        disperseStowedMessages();
        throw new StorageException(cause.result, message, cause);
      });
  }

  //==================================
  // internal implementation
  //==================================

  /**
   * Apply an individual {@code source} onto my concrete extender by means of
   * the {@code BiConsumer} of its registered {@code sourcedTypeMap}.
   * @param source the {@code Source<STT>} to apply
   */
  private <STT> void applyResultVersioned(final Source<STT> source) {
    final Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(getClass());

    if (sourcedTypeMap == null) {
      disperseStowedMessages();
      throw new IllegalStateException("No such Sourced type.");
    }

    BiConsumer<Sourced<?>, Source<?>> consumer = sourcedTypeMap.get(source.getClass());

    consumer.accept(this, source);

    ++currentVersion;
  }

  /**
   * Given that the {@code supplier} is non-null, execute it by completing the {@code CompletionSupplier<?>}.
   * @param supplier the {@code CompletionSupplier<?>} or null
   */
  private void completeUsing(final Object supplier) {
    if (supplier != null) {
      ((CompletionSupplier<?>) supplier).complete();
    }
  }

  /**
   * Restore the state of my concrete extender from a possibly snaptshot
   * and stream of events.
   */
  private void restore() {
    stowMessages();

    journalInfo.journal.streamReader(getClass().getSimpleName())
      .andThenTo(reader -> reader.streamFor(streamName()))
      .andThenConsume(stream -> {
        restoreSnapshot(stream.snapshot);
        restoreFrom(journalInfo.entryAdapterProvider.asSources(stream.entries), stream.streamVersion);
        disperseStowedMessages();
      })
      .otherwiseConsume(stream -> {
        disperseStowedMessages();
      })
      .recoverFrom(cause -> {
        final String message = "Stream not recovered for: " + type() + "(" + streamName() + ") because: " + cause.getMessage();
        disperseStowedMessages();
        throw new StorageException(Result.Failure, message, cause);
      });
  }

  /**
   * Restore the state of my concrete extender from the {@code stream} and
   * set my {@code currentVersion}.
   * @param stream the {@code List<Source<T>> stream} from which state is restored
   * @param currentVersion the int to set as my currentVersion
   */
  private void restoreFrom(final List<Source<T>> stream, final int currentVersion) {
    final Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(getClass());

    if (sourcedTypeMap == null) {
      throw new IllegalStateException("No such Sourced type.");
    }

    for (final Source<?> source : stream) {
      sourcedTypeMap.get(source.getClass()).accept(this, source);
    }

    this.currentVersion = currentVersion;
  }

  /**
   * Restores the initial state of the receiver by means of the {@code snapshot}.
   * @param snapshot the {@code Optional<SNAPSHOT>} holding the {@code Sourced<T,ST>} initial state
   */
  private void restoreSnapshot(final State<?> snapshot) {
    if (snapshot != null) {
      this.restoreSnapshot(journalInfo.stateAdapterProvider.fromRaw(snapshot), currentVersion);
    }
  }

  /**
   * Answer my type name.
   * @return String
   */
  private String type() {
    return getClass().getSimpleName();
  }

  /**
   * Answer {@code source} wrapped in a {@code List<Source<T>>}.
   * @param source the {@code Source<T>} to wrap
   * @return {@code List<Source<T>>}
   */
  private List<Source<T>> wrap(final Source<T> source) {
    return Arrays.asList(source);
  }

  /**
   * Answer {@code sources} wrapped in a {@code List<Source<T>>}.
   * @param source the {@code Source<T>[]} to wrap
   * @return {@code List<Source<T>>}
   */
  @SuppressWarnings("unused")
  private List<Source<T>> wrap(final Source<T>[] sources) {
    return Arrays.asList(sources);
  }

  /**
   * Answer {@code sources} wrapped in a {@code List<Source<T>>}.
   * @param source the {@code List<Source<T>>} to wrap
   * @return {@code List<Source<T>>}
   */
  private List<Source<T>> wrap(final List<Source<T>> sources) {
    return new ArrayList<>(sources);
  }
}
