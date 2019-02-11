// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.vlingo.lattice.model.Command;
import io.vlingo.lattice.model.DomainEvent;
import io.vlingo.lattice.model.process.ProcessTypeRegistry.SourcedProcessInfo;
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
 * than the {@code emit()} behaviors.
 */
public abstract class SourcedProcess extends Sourced<ProcessMessage> implements Process {
  private final SourcedProcessInfo info;

  /**
   * @see io.vlingo.lattice.model.process.Process#send(io.vlingo.lattice.model.Command)
   */
  @Override
  public void send(final Command command) {
    // TODO: send
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#send(io.vlingo.lattice.model.DomainEvent)
   */
  @Override
  public void send(final DomainEvent event) {
    // TODO: send
  }

  /**
   * Construct my default state.
   */
  protected SourcedProcess() {
    this.info = null; // TODO
  }

  /**
   * Emit the {@code command} by applying it to myself.
   * @param command the Command to apply
   */
  protected void emit(final Command command) {
    apply(new ProcessMessage(command));
  }

  /**
   * Emit the {@code command} by applying it to myself, followed by
   * the execution of a possible {@code andThen}.
   * @param command the Command to apply
   * @param andThen the {@code Supplier<R>} executed following the application of command
   * @param <R> the return type of the andThen {@code Supplier<R>}
   */
  protected <R> void emit(final Command command, final Supplier<R> andThen) {
    apply(new ProcessMessage(command), andThen);
  }

  /**
   * Emit the {@code event} by applying it to myself.
   * @param event the DomainEvent to apply
   */
  protected void emit(final DomainEvent event) {
    apply(new ProcessMessage(event));
  }

  /**
   * Emit the {@code event} by applying it to myself, followed by
   * the execution of a possible {@code andThen}.
   * @param event the DomainEvent to apply
   * @param andThen the {@code Supplier<R>} executed following the application of event
   * @param <R> the return type of the andThen {@code Supplier<R>}
   */
  protected <R> void emit(final DomainEvent event, final Supplier<R> andThen) {
    apply(new ProcessMessage(event), andThen);
  }

  /**
   * Emit all {@code sources} by applying them to myself.
   * @param sources the {@code List<Source<?>>} of source instances to apply
   */
  protected void emitAll(final List<Source<?>> sources) {
    apply((List<Source<ProcessMessage>>) wrap(sources));
  }

  /**
   * Emit all {@code sources} by applying them to myself, followed by
   * the execution of a possible {@code andThen}.
   * @param sources the {@code List<Source<?>>} of source instances to apply
   * @param andThen the {@code Supplier<R>} executed following the application of sources
   * @param <R> the return type of the andThen {@code Supplier<R>}
   */
  protected <R> void emitAll(final List<Source<?>> sources, final Supplier<R> andThen) {
    apply((List<Source<ProcessMessage>>) wrap(sources), andThen);
  }

  /**
   * Answer a new {@code List<Source<ProcessMessage>>} that wraps each of the elements of {@code sources}.
   * @param sources the {@code List<Source<?>>} elements each to be wrapped with a ProcessMessage
   * @return {@code List<Source<ProcessMessage>>}
   */
  private List<Source<ProcessMessage>> wrap(final List<Source<?>> sources) {
    final List<Source<ProcessMessage>> messages = new ArrayList<>(sources.size());
    for (final Source<?> source : sources) {
      final ProcessMessage message = new ProcessMessage(source);
      messages.add(message);
    }
    return messages;
  }
}
