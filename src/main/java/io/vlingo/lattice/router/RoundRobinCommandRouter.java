// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.actors.Definition;
import io.vlingo.actors.RoundRobinRouter;
import io.vlingo.actors.RouterSpecification;

/**
 * The {@code CommandRouter} implementation for round-robin routing.
 */
public class RoundRobinCommandRouter extends RoundRobinRouter<CommandRouter> implements CommandRouter {
  /**
   * Constructs my default state.
   * @param totalRoutees the int number or routees to create
   */
  public RoundRobinCommandRouter(final int totalRoutees) {
    super(new RouterSpecification<CommandRouter>(
            totalRoutees,
            Definition.has(CommandRouterWorkerActor.class, Definition.NoParameters),
            CommandRouter.class));
  }

  /**
   * Route to next routee.
   * @see io.vlingo.lattice.router.CommandRouter#route(io.vlingo.lattice.router.RoutableCommand)
   */
  @Override
  public <P, A> void route(final RoutableCommand<P, A> command) {
    dispatchCommand(CommandRouter::route, command);
  }
}
