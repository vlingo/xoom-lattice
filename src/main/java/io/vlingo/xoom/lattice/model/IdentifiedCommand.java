// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model;

/**
 * Provides the means to request the identity of the {@code Command}.
 */
public abstract class IdentifiedCommand extends Command {
  /**
   * Construct my default state with a type version of 1.
   */
  public IdentifiedCommand() {
    super();
  }

  /**
   * Construct my default state with a {@code eventTypeVersion} greater than 1.
   * @param eventTypeVersion the int version of this event type
   */
  public IdentifiedCommand(final int eventTypeVersion) {
    super(eventTypeVersion);
  }

  /**
   * Answer the {@code String} identity of this {@code Command}.
   * @return String
   */
  public abstract String identity();
}
