// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Stoppable;
import io.vlingo.actors.testkit.TestContext;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.common.Completes;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.ApplyFailedException;
import io.vlingo.lattice.model.ApplyFailedException.Applicable;
import io.vlingo.lattice.model.CompletionSupplier;
import io.vlingo.symbio.Metadata;
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
   * @param sourcedType the concrete {@code Class<SOURCED>} type to which sourceType instances are applied
   * @param sourceType the concrete {@code Class<SOURCE>} type to apply
   * @param consumer the {@code BiConsumer<SOURCED, SOURCE>} used to perform the application of sourceType
   * @param <SOURCED> the type {@code <? extends Sourced<?>>} of the sourced entity to apply to
   * @param <SOURCE> the type {@code <? extends Source<?>>} of the source to be applied
   */
  @SuppressWarnings("unchecked")
  public static <SOURCED extends Sourced<?>, SOURCE extends Source<?>> void registerConsumer(
          final Class<SOURCED> sourcedType,
          final Class<SOURCE> sourceType,
          final BiConsumer<SOURCED, SOURCE> consumer) {

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
      testContext.initialReferenceValueOf(new CopyOnWriteArrayList<>());
    }
  }

  /*
   * @see io.vlingo.actors.Actor#viewTestState()
   */
  @Override
  public TestState viewTestState() {
    final TestState testState = new TestState();
    testState.putValue("applied", testContext.referenceValue());
    return testState;
  }

  /**
   * Construct my default state.
   */
  protected Sourced() {
    this.currentVersion = 0;
    this.journalInfo = info();
    this.interest = selfAs(AppendResultInterest.class);
  }

  /**
   * Apply all of the given {@code sources} to myself, which includes appending
   * them to my journal and reflecting the representative changes to my state.
   * @param sources the {@code List<Source<T>>} to apply
   */
  final protected void apply(final List<Source<T>> sources) {
    apply(sources, metadata(), null);
  }

  /**
   * Answer {@code Completes<RT>}, applying all of the given {@code sources} to myself,
   * which includes appending them to my journal and reflecting the representative changes
   * to my state, followed by the execution of a possible {@code andThen}.
   * @param sources the {@code List<Source<T>>} to apply
   * @param andThen the {@code Supplier<R>} executed following the application of sources
   * @param <R> the return type of the andThen {@code Supplier<R>}
   * @return {@code Completes<R>}
   */
  final protected <R> Completes<R> apply(final List<Source<T>> sources, final Supplier<R> andThen) {
    return apply(sources, metadata(), andThen);
  }

  /**
   * Answer {@code Completes<RT>}, applying all of the given {@code sources} to myself,
   * which includes appending them to my journal and reflecting the representative changes
   * to my state, followed by the execution of a possible {@code andThen}.
   * @param sources the {@code List<Source<T>>} to apply
   * @param metadata the Metadata to apply along with source
   * @param andThen the {@code Supplier<R>} executed following the application of sources
   * @param <R> the return type of the andThen {@code Supplier<R>}
   * @return {@code Completes<R>}
   */
  final protected <R> Completes<R> apply(final List<Source<T>> sources, final Metadata metadata, final Supplier<R> andThen) {
    beforeApply(sources);
    final Journal<?> journal = journalInfo.journal();
    stowMessages(AppendResultInterest.class);
    journal.appendAllWith(streamName(), nextVersion(), sources, metadata, snapshot(), interest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
    return andThen == null ? null : completes();
  }

  /**
   * Apply the given {@code source} to myself, which includes appending it
   * to my journal and reflecting the representative changes to my state.
   * @param source the {@code Source<T>} to apply
   */
  final protected void apply(final Source<T> source) {
    apply(source, metadata(), null);
  }

  /**
   * Answer {@code Completes<R>}, applying the given {@code source} to myself, which
   * includes appending it to my journal and reflecting the representative changes to my
   * state, followed by the execution of a possible {@code andThen}.
   * @param source the {@code Source<T>} to apply
   * @param andThen the {@code Supplier<R>} executed following the application of sources
   * @param <R> the return type of the andThen {@code Supplier<R>}
   * @return {@code Completes<R>}
   */
  final protected <R> Completes<R> apply(final Source<T> source, final Supplier<R> andThen) {
    return apply(source, metadata(), andThen);
  }

  /**
   * Answer {@code Completes<R>}, applying the given {@code source} to myself, which
   * includes appending it to my journal and reflecting the representative changes to my
   * state, followed by the execution of a possible {@code andThen}.
   * @param source the {@code Source<T>} to apply
   * @param metadata the Metadata to apply along with source
   * @param andThen the {@code Supplier<R>} executed following the application of sources
   * @param <R> the return type of the andThen {@code Supplier<R>}
   * @return {@code Completes<R>}
   */
  final protected <R> Completes<R> apply(final Source<T> source, final Metadata metadata, final Supplier<R> andThen) {
    final List<Source<T>> toApply = wrap(source);
    beforeApply(toApply);
    final Journal<?> journal = journalInfo.journal();
    stowMessages(AppendResultInterest.class);
    journal.appendAllWith(streamName(), nextVersion(), toApply, metadata, snapshot(), interest, CompletionSupplier.supplierOrNull(andThen, completesEventually()));
    return andThen == null ? null : completes();
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
   * Received after the full asynchronous evaluation of each {@code apply()}.
   * Override if notification is desired.
   */
  protected void afterApply() { }

  /**
   * Answer {@code Optional<ApplyFailedException>} that should be thrown
   * and handled by my {@code Supervisor}, unless it is empty. The default
   * behavior is to answer the given {@code exception}, which will be thrown.
   * Must override to change default behavior.
   * @param exception the ApplyFailedException
   * @return {@code Optional<ApplyFailedException>}
   */
  protected Optional<ApplyFailedException> afterApplyFailed(final ApplyFailedException exception) {
    return Optional.of(exception);
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
      final List<Source<T>> all = testContext.referenceValue();
      all.addAll(sources);
      testContext.referenceValueTo(all);
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
   * Answer my {@code Metadata}.
   * Must override if {@code Metadata} is to be supported.
   * @return Metadata
   */
  protected Metadata metadata() {
    return Metadata.nullMetadata();
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
  public final <S, ST> void appendResultedIn(
          final Outcome<StorageException, io.vlingo.symbio.store.Result> outcome, final String streamName, final int streamVersion,
          final Source<S> source, final Optional<ST> snapshot, final Object supplier) {
      this.appendResultedIn(outcome, streamName, streamVersion, source, Metadata.nullMetadata(), snapshot, supplier);
  }

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  public final <S, ST> void appendAllResultedIn(final Outcome<StorageException, io.vlingo.symbio.store.Result> outcome, final String streamName,
          final int streamVersion, final List<Source<S>> sources, final Optional<ST> snapshot, final Object supplier) {
    this.appendAllResultedIn(outcome, streamName, streamVersion, sources, Metadata.nullMetadata(), snapshot, supplier);
  }


  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  public final <S, STT> void appendResultedIn(
          final Outcome<StorageException, Result> outcome, final String streamName, final int streamVersion,
          final Source<S> source, final Metadata metadata, final Optional<STT> snapshot, final Object supplier) {
    //TODO handle metadata
    outcome
      .andThen(result -> {
        restoreSnapshot(snapshot, currentVersion);
        applyResultVersioned(source);
        afterApply();
        completeUsing(supplier);
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final Applicable<?> applicable = new Applicable<>(null, Arrays.asList(source), metadata, (CompletionSupplier<?>) supplier);
        final String message = "Source (count 1) not appended for: " + type() + "(" + streamName() + ") because: " + cause.result + " with: " + cause.getMessage();
        final ApplyFailedException exception = new ApplyFailedException(applicable, message, cause);
        final Optional<ApplyFailedException> maybeException = afterApplyFailed(exception);
        disperseStowedMessages();
        if (maybeException.isPresent()) {
          logger().error(message, maybeException.get());
          throw maybeException.get();
        }
        logger().error(message, exception);
        return cause.result;
      });
  }

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public final <STT, ST> void appendAllResultedIn(final Outcome<StorageException, Result> outcome, final String streamName,
          final int streamVersion,final List<Source<STT>> sources, final Metadata metadata,
          final Optional<ST> snapshot, final Object supplier) {
    //TODO handle metadata
    outcome
      .andThen(result -> {
        restoreSnapshot(snapshot, currentVersion);
        for (final Source<STT> source : sources) {
          applyResultVersioned(source);
        }
        afterApply();
        completeUsing(supplier);
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final Applicable<?> applicable = new Applicable(null, sources, metadata, (CompletionSupplier<?>) supplier);
        final String message = "Source (count " + sources.size() + ") not appended for: " + type() + "(" + streamName() + ") because: " + cause.result + " with: " + cause.getMessage();
        final ApplyFailedException exception = new ApplyFailedException(applicable, message, cause);
        final Optional<ApplyFailedException> maybeException = afterApplyFailed(exception);
        disperseStowedMessages();
        if (maybeException.isPresent()) {
          logger().error(message, maybeException.get());
          throw maybeException.get();
        }
        logger().error(message, exception);
        return cause.result;
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

  private SourcedTypeRegistry.Info<?> info() {
    try {
      return stage().world().resolveDynamic(SourcedTypeRegistry.INTERNAL_NAME, SourcedTypeRegistry.class).info(getClass());
    } catch (Exception e) {
      final String message = getClass().getSimpleName() + ": Info not registered with SourcedTypeRegistry.";
      logger().error(message);
      throw new IllegalStateException(message);
    }
  }

  /**
   * Restore the state of my concrete extender from a possibly snaptshot
   * and stream of events.
   */
  private void restore() {
    stowMessages(Stoppable.class);

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
        disperseStowedMessages();
        final String message = "Stream not recovered for: " + type() + "(" + streamName() + ") because: " + cause.getMessage();
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
    if (snapshot != null && !snapshot.isNull()) {
      restoreSnapshot(journalInfo.stateAdapterProvider.fromRaw(snapshot), currentVersion);
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
   * @param sources the {@code Source<T>[]} to wrap
   * @return {@code List<Source<T>>}
   */
  @SuppressWarnings("unused")
  private List<Source<T>> wrap(final Source<T>[] sources) {
    return Arrays.asList(sources);
  }
}
