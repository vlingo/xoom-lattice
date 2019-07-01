// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.lattice.model.Command;
import io.vlingo.lattice.model.DomainEvent;
import io.vlingo.symbio.Source;

/**
 * A {@code Source} for both {@code Command} and {@code DomainEvent} types,
 * but that supports other {@code Source} not previously known.
 */
public class ProcessMessage extends Source<ProcessMessage> {
  public Source<?> source;


  /**
   * Answer a new {@code List<Source<ProcessMessage>>} that wraps each of the elements of {@code sources}.
   * @param sources the {@code List<Source<C>>} elements each to be wrapped with a ProcessMessage
   * @param <C> the type of Source
   * @return {@code List<Source<ProcessMessage>>}
   */
  public static <C> List<Source<ProcessMessage>> wrap(final List<Source<C>> sources) {
    final boolean reuse = (!sources.isEmpty() && sources.get(0).getClass() == ProcessMessage.class);

    final List<Source<ProcessMessage>> messages = new ArrayList<>(sources.size());
    for (final Source<?> source : sources) {
      if (reuse) {
        messages.add((ProcessMessage) source);
      } else {
        final ProcessMessage message = new ProcessMessage(source);
        messages.add(message);
      }
    }
    return messages;
  }

  /**
   * Construct my default state with the {@code command} and a type version of 1.
   * @param command the Command to set as my source
   */
  public ProcessMessage(final Command command) {
    super();

    this.source = command;
  }

  /**
   * Construct my default state with an {@code event} and a type version of 1.
   * @param event the Event to set as my source
   */
  public ProcessMessage(final DomainEvent event) {
    super();

    this.source = event;
  }

  /**
   * Construct my default state with a {@code source} and a type version of 1.
   * @param source the {@code Source<?>} to set as my source
   */
  public ProcessMessage(final Source<?> source) {
    super();

    this.source = source;
  }

  /**
   * Construct my default state with {@code null} and a type version of 1.
   */
  public ProcessMessage() {
    super();

    this.source = null;
  }

  /**
   * Answer the {@code id} of my {@code source}. For use by a
   * {@code Process}, every ProcessMessage must provide a valid
   * {@code id} for workflow correlation, and this {@code id}
   * must match the {@code id} of the {@code Process}.
   * @return String
   * @see io.vlingo.symbio.Source#id()
   */
  @Override
  public String id() {
    return source.id();
  }

  /**
   * Answer the type of my {@code source} as a {@code Class<?>}.
   * @return {@code Class<?>}
   */
  public Class<?> sourceType() {
    return source.getClass();
  }

  /**
   * Answer the type name of my {@code source} as a {@code String}.
   * @return String
   */
  public String sourceTypeName() {
    return source.getClass().getName();
  }

  /**
   * Answer my {@code source} as an instance of type {@code T}.
   * @param <T> the type of Source
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T extends Source<?>> T typedSource() {
    return (T) source;
  }
}
