// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.lattice.model.Command;
import io.vlingo.lattice.model.DomainEvent;
import io.vlingo.lattice.model.object.ObjectEntity;

/**
 * Abstract base definition for all concrete object process types.
 * @param <T> the type of ObjectEntity
 */
public abstract class ObjectProcess<T> extends ObjectEntity<T> implements Process {

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
}
