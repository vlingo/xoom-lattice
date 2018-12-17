// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.common.Outcome;
import io.vlingo.common.collection.ResettableReadOnlyList;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.Journal.AppendResultInterest;

public abstract class Sourced<T> extends Actor implements AppendResultInterest<T> {
  private static final Map<Class<Sourced<Source<?>>>,Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>>> registeredConsumers =
          new ConcurrentHashMap<>();

  private final ResettableReadOnlyList<Source<T>> applied;
  private int currentVersion;
  private SourcedTypeRegistry.Info<?,?> journalInfo;
  private AppendResultInterest<T> interest;

  @SuppressWarnings("unchecked")
  public static void registerConsumer(
          Class<? extends Sourced<?>> sourcedType,
          Class<? extends Source<?>> sourceType,
          BiConsumer<? extends Sourced<?>, ? extends Source<?>> consumer) {

    Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(sourcedType);

    if (sourcedTypeMap == null) {
      sourcedTypeMap = new HashMap<>();
      registeredConsumers.put((Class<Sourced<Source<?>>>) sourcedType, sourcedTypeMap);
    }

    sourcedTypeMap.put((Class<Source<?>>) sourceType, (BiConsumer<Sourced<?>, Source<?>>) consumer);
  }

  @Override
  public TestState viewTestState() {
    final TestState testState = new TestState();
    testState.putValue("applied", applied);
    return testState;
  }

  @SuppressWarnings("unchecked")
  protected Sourced() {
    this.applied = new ResettableReadOnlyList<>();
    this.currentVersion = 0;
    this.journalInfo = SourcedTypeRegistry.instance.info(getClass());
    this.interest = selfAs(AppendResultInterest.class);
  }

  protected Sourced(final List<Source<T>> stream, final int currentVersion) {
    this();

    final Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
            registeredConsumers.get(getClass());

    if (sourcedTypeMap == null) {
      throw new IllegalStateException("No such Sourced type.");
    }

    for (final Source<?> source : stream) {
      sourcedTypeMap.get(source.getClass()).accept(this, source);
    }
  }

  protected Sourced(final List<Source<T>> stream, final int currentVersion, final Consumer<Source<?>> consumer) {
    this();

    for (final Source<?> source : stream) {
      consumer.accept(source);
    }
  }

  @SafeVarargs
  final protected void apply(final Source<T>... sources) {
    applied.wrap(sources);
    final Journal<T> journal = journalInfo.journal();
    stowMessages(AppendResultInterest.class);
    journal.appendAll(streamName(), nextVersion(), applied, interest, null);
  }

  final protected void apply(final Source<T> source, final Consumer<Source<T>> consumer) {
    applied.wrap(source);
    final Journal<T> journal = journalInfo.journal();
    stowMessages(AppendResultInterest.class);
    journal.append(streamName(), nextVersion(), source, interest, consumer);
  }

  protected abstract String streamName();

  //==================================
  // AppendResultInterest
  //==================================

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  @SuppressWarnings("unchecked")
  final public <S> void appendResultedIn(
          final Outcome<StorageException, Result> outcome,
          final String streamName,
          final int streamVersion,
          final Source<S> source,
          final Optional<State<T>> snapshot,
          final Object consumer) {

    outcome
      .andThen(result -> {
        ((Consumer<Source<?>>) consumer).accept(source);
        ++currentVersion;
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "Source (count 1) not appended for: " + type() + "(" + streamName() + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().log(message, cause);
        throw new StorageException(cause.result, message, cause);
      });
  }

  /**
   * FOR INTERNAL USE ONLY.
   */
  @Override
  final public <S> void appendAllResultedIn(
          final Outcome<StorageException, Result> outcome,
          final String streamName,
          final int streamVersion,
          final List<Source<S>> sources,
          final Optional<State<T>> snapshot,
          final Object object) {

    outcome
      .andThen(result -> {
        final Map<Class<Source<?>>, BiConsumer<Sourced<?>, Source<?>>> sourcedTypeMap =
                registeredConsumers.get(getClass());

        if (sourcedTypeMap == null) {
          throw new IllegalStateException("No such Sourced type.");
        }

        for (final Source<S> source : sources) {
          BiConsumer<Sourced<?>, Source<?>> consumer = sourcedTypeMap.get(source.getClass());
          consumer.accept(this, source);
          ++currentVersion;
        }
        disperseStowedMessages();
        return result;
      })
      .otherwise(cause -> {
        final String message = "Source (count " + applied.size() + ") not appended for: " + type() + "(" + streamName() + ") because: " + cause.result + " with: " + cause.getMessage();
        logger().log(message, cause);
        throw new StorageException(cause.result, message, cause);
      });
  }

  //==================================
  // internal implementation
  //==================================

  private int nextVersion() {
    return currentVersion + 1;
  }

  private String type() {
    return getClass().getSimpleName();
  }
}
