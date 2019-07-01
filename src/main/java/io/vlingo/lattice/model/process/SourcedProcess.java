// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import java.util.List;
import java.util.function.Supplier;

import io.vlingo.lattice.model.Command;
import io.vlingo.lattice.model.DomainEvent;
import io.vlingo.lattice.model.sourcing.Sourced;
import io.vlingo.symbio.Source;

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

  /**
   * @see io.vlingo.lattice.model.process.Process#chronicle()
   */
  @Override
  public Chronicle<T> chronicle() {
    return snapshot();
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.Command)
   */
  @Override
  public void emit(final Command command) {
    apply(new ProcessMessage(command));
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.Command, java.util.function.Supplier)
   */
  @Override
  public <R> void emit(final Command command, final Supplier<R> andThen) {
    apply(new ProcessMessage(command), andThen);
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.DomainEvent)
   */
  @Override
  public void emit(final DomainEvent event) {
    apply(new ProcessMessage(event));
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.DomainEvent, java.util.function.Supplier)
   */
  @Override
  public <R> void emit(final DomainEvent event, final Supplier<R> andThen) {
    apply(new ProcessMessage(event), andThen);
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.lattice.model.process.Process#emitAll(java.util.List)
   */
  @Override
  public <C> void emitAll(final List<Source<C>> sources) {
    apply(ProcessMessage.wrap(sources));
  }

  /**
   * Uses the underlying {@code Journal} for Event Sourcing semantics.
   * @see io.vlingo.lattice.model.process.Process#emitAll(java.util.List, java.util.function.Supplier)
   */
  @Override
  public <C,R> void emitAll(final List<Source<C>> sources, final Supplier<R> andThen) {
    apply(ProcessMessage.wrap(sources), andThen);
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#send(io.vlingo.lattice.model.Command)
   */
  @Override
  public void send(final Command command) {
    info.exchange.send(command);
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#send(io.vlingo.lattice.model.DomainEvent)
   */
  @Override
  public void send(final DomainEvent event) {
    info.exchange.send(event);
  }

  /**
   * Construct my default state.
   */
  protected SourcedProcess() {
    this.info = stage().world().resolveDynamic(ProcessTypeRegistry.INTERNAL_NAME, ProcessTypeRegistry.class).info(getClass());
  }
}
