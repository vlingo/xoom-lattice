// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.lattice.model.Command;
import io.vlingo.lattice.model.DomainEvent;

/**
 * Definition for a long-running process.
 */
public interface Process {
  /**
   * Answer my id, which is used for correlation among my collaborators.
   * @return String
   */
  String id();

  /**
   * Send the {@code command} to my collaborators via my Exchange.
   * @param command the Command to send
   */
  void send(final Command command);

  /**
   * Send the {@code even} to my collaborators via my Exchange.
   * @param event the DomainEvent to apply
   */
  void send(final DomainEvent event);
}
