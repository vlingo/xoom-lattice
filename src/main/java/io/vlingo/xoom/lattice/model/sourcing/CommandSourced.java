// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.sourcing;

import io.vlingo.xoom.lattice.model.Command;

/**
 * A {@code Sourced<T>} for concrete types of {@code Command}.
 */
public abstract class CommandSourced extends Sourced<Command> {
  public CommandSourced() {
    super();
  }

  public CommandSourced(final String streamName) {
    super(streamName);
  }
}
