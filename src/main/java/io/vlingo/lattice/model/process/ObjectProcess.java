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
import io.vlingo.lattice.model.object.ObjectEntity;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.store.object.PersistentObject;

/**
 * Abstract base definition for all concrete object process types.
 * @param <T> the type of ObjectEntity
 */
public abstract class ObjectProcess<T extends PersistentObject> extends ObjectEntity<T> implements Process<T> {

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.Command)
   */
  @Override
  public void emit(final Command command) {
    // TODO: emit
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.Command, java.util.function.Supplier)
   */
  @Override
  public <R> void emit(final Command command, final Supplier<R> andThen) {
    // TODO: emit
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.DomainEvent)
   */
  @Override
  public void emit(final DomainEvent event) {
    // TODO: emit
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emit(io.vlingo.lattice.model.DomainEvent, java.util.function.Supplier)
   */
  @Override
  public <R> void emit(final DomainEvent event, final Supplier<R> andThen) {
    // TODO: emit
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emitAll(java.util.List)
   */
  @Override
  public void emitAll(final List<Source<?>> sources) {
    // TODO: emit
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#emitAll(java.util.List, java.util.function.Supplier)
   */
  @Override
  public <R> void emitAll(final List<Source<?>> sources, final Supplier<R> andThen) {
    // TODO: emit
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#send(io.vlingo.lattice.model.Command)
   */
  @Override
  public void send(final Command command) {
    // TODO: send
    // info.exchange.send(command);
  }

  /**
   * @see io.vlingo.lattice.model.process.Process#send(io.vlingo.lattice.model.DomainEvent)
   */
  @Override
  public void send(final DomainEvent event) {
    // TODO: send
    // info.exchange.send(event);
  }
}
