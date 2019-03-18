// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

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

  public ProcessMessage() {
    super();

    this.source = null;
  }

  public String sourceTypeName() {
    return source.getClass().getName();
  }
}
