// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.lattice.model.Command;
import io.vlingo.lattice.model.DomainEvent;
import io.vlingo.lattice.model.stateful.StatefulEntity;

/**
 * Abstract base definition for all concrete stateful process types.
 * @param <T> the type of StatefulEntity
 */
public abstract class StatefulProcess<T> extends StatefulEntity<T> implements Process {

  @Override
  public void send(final Command command) {
    // TODO: send
  }

  @Override
  public void send(final DomainEvent event) {
    // TODO: send
  }
}
