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
import io.vlingo.lattice.model.stateful.StatefulEntity;
import io.vlingo.symbio.Source;

/**
 * Abstract base definition for all concrete stateful process types.
 * @param <T> the type of StatefulEntity
 */
public abstract class StatefulProcess<T> extends StatefulEntity<T> implements Process<T> {
  private final ProcessTypeRegistry.Info<? extends SourcedProcess<T>> info;

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.Command)
   */
  @Override
  public void emit(final Command command) {
    apply(chronicle().state, new ProcessMessage(command));
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.Command, java.util.function.Supplier)
   */
  @Override
  public <R> void emit(final Command command, final Supplier<R> andThen) {
    apply(chronicle().state, new ProcessMessage(command), andThen);
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.DomainEvent)
   */
  @Override
  public void emit(final DomainEvent event) {
    apply(chronicle().state, new ProcessMessage(event));
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.DomainEvent, java.util.function.Supplier)
   */
  @Override
  public <R> void emit(final DomainEvent event, final Supplier<R> andThen) {
    apply(chronicle().state, new ProcessMessage(event), andThen);
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emitAll(java.util.List)
   */
  @Override
  public <C> void emitAll(final List<Source<C>> sources) {
    apply(chronicle().state, ProcessMessage.wrap(sources));
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emitAll(java.util.List, java.util.function.Supplier)
   */
  @Override
  public <C,R> void emitAll(final List<Source<C>> sources, final Supplier<R> andThen) {
    apply(chronicle().state, ProcessMessage.wrap(sources), andThen);
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

  protected StatefulProcess() {
    this.info = stage().world().resolveDynamic(ProcessTypeRegistry.INTERNAL_NAME, ProcessTypeRegistry.class).info(getClass());
  }
}
