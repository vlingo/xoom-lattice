// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Stage;
import io.vlingo.lattice.model.Command;

/**
 * The routee actor responsible for routing commands to its handler.
 */
public class CommandRouterWorkerActor extends Actor implements CommandRouter {
  private final Stage stage;

  /**
   * Constructs my default state.
   */
  public CommandRouterWorkerActor() {
    this.stage = stage();
  }

  /**
   * @see io.vlingo.lattice.router.CommandRouter#route(io.vlingo.lattice.router.RoutableCommand)
   */
  @Override
  public <P,C extends Command,A> void route(final RoutableCommand<P,C,A> command) {
    command.handleWithin(stage);
  }
}
