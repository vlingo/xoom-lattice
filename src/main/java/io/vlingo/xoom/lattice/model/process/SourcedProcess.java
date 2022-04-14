// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.Command;
import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.Sourced;
import io.vlingo.xoom.symbio.Source;

/**
 * Abstract base definition for all concrete sourced process types. My extenders
 * {@code emit()} {@code Command} and/or {@code DomainEvent} instances
 * that cause reactions by my process collaborators. The underlying support
 * comes from the {@code Sourced<T>} base, with {@code ProcessMessage} serving
 * as the {@code T}. Thus, every emitted {@code Command} and {@code DomainEvent}
 * is wrapped by a {@code ProcessMessage}. Due to the fact that I am effectively
 * dual sourced, my state is comprised of a stream of all emitted instances of
 * {@code Source<T>} types. In case you do not desire a given {@code Source<T>}
 * to contribute to my state, use the {@code send()} behaviors for those rather
 * than the {@code emit()} behaviors. Note, however, that {@code send()} is
 * subject to failures of the underlying {@code Exchange} mechanism.
 * @param <T> the type of the process state and used by the {@code Chronicle<T>}
 */
public abstract class SourcedProcess<T> extends Sourced<ProcessMessage> implements Process<T> {
  private final ProcessTypeRegistry.Info<? extends SourcedProcess<T>> info;

  private List<Source<?>> applied;

  /**
   * Construct my default state using my {@code address} as my {@code streamName}.
   */
  protected SourcedProcess() {
    this(null);
  }

  /**
   * Construct my default state.
   * @param streamName the String unique identity of this entity
   */
  protected SourcedProcess(final String streamName) {
    super(streamName);
    this.info = stage().world().resolveDynamic(ProcessTypeRegistry.INTERNAL_NAME, ProcessTypeRegistry.class).info(getClass());
    this.applied = new ArrayList<>(2);
  }

  /**
   * @see io.vlingo.xoom.lattice.model.process.Process#chronicle()
   */
  @Override
  public Chronicle<T> chronicle() {
    return snapshot();
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.xoom.lattice.model.process.Process#process(io.vlingo.xoom.lattice.model.Command)
   */
  @Override
  public void process(final Command command) {
    applied.add(command);
    apply(new ProcessMessage(command));
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.xoom.lattice.model.process.Process#process(io.vlingo.xoom.lattice.model.Command, java.util.function.Supplier)
   */
  @Override
  public <R> Completes<R> process(final Command command, final Supplier<R> andThen) {
    applied.add(command);
    return apply(new ProcessMessage(command), andThen);
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.xoom.lattice.model.process.Process#process(io.vlingo.xoom.lattice.model.DomainEvent)
   */
  @Override
  public void process(final DomainEvent event) {
    applied.add(event);
    apply(new ProcessMessage(event));
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.xoom.lattice.model.process.Process#process(io.vlingo.xoom.lattice.model.DomainEvent, java.util.function.Supplier)
   */
  @Override
  public <R> Completes<R> process(final DomainEvent event, final Supplier<R> andThen) {
    applied.add(event);
    return apply(new ProcessMessage(event), andThen);
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.xoom.lattice.model.process.Process#processAll(java.util.List)
   */
  @Override
  public <C> void processAll(final List<Source<C>> sources) {
    applied.addAll(sources);
    apply(ProcessMessage.wrap(sources));
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.xoom.lattice.model.process.Process#processAll(java.util.List, java.util.function.Supplier)
   */
  @Override
  public <C,R> Completes<R> processAll(final List<Source<C>> sources, final Supplier<R> andThen) {
    applied.addAll(sources);
    return apply(ProcessMessage.wrap(sources), andThen);
  }

  /**
   * @see io.vlingo.xoom.lattice.model.process.Process#send(io.vlingo.xoom.lattice.model.Command)
   */
  @Override
  public void send(final Command command) {
    info.exchange.send(command);
  }

  /**
   * @see io.vlingo.xoom.lattice.model.process.Process#send(io.vlingo.xoom.lattice.model.DomainEvent)
   */
  @Override
  public void send(final DomainEvent event) {
    info.exchange.send(event);
  }

  @Override
  protected void afterApply() {
    for (final Source<?> source : applied) {
      info.exchange.send(source);
    }
    applied.clear();
  }
}
