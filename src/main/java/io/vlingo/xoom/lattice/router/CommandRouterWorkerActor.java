// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.router;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.lattice.model.Command;

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
   * @see io.vlingo.xoom.lattice.router.CommandRouter#route(io.vlingo.xoom.lattice.router.RoutableCommand)
   */
  @Override
  public <P,C extends Command,A> void route(final RoutableCommand<P,C,A> command) {
    command.handleWithin(stage);
  }
}
