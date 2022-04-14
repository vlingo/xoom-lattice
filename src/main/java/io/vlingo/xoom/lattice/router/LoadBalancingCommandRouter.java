// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.router;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.RouterSpecification;
import io.vlingo.xoom.actors.SmallestMailboxRouter;
import io.vlingo.xoom.lattice.model.Command;

/**
 * The {@code CommandRouter} implementation for load-balancing on the {@code RoutableCommand}.
 */
public class LoadBalancingCommandRouter extends SmallestMailboxRouter<CommandRouter> implements CommandRouter {
  /**
   * Constructs my default state.
   * @param totalRoutees the int number or routees to create
   */
  public LoadBalancingCommandRouter(final int totalRoutees) {
    super(new RouterSpecification<CommandRouter>(
            totalRoutees,
            Definition.has(CommandRouterWorkerActor.class, Definition.NoParameters),
            CommandRouter.class));
  }

  /**
   * Route to least busy routee.
   * @see io.vlingo.xoom.lattice.router.CommandRouter#route(io.vlingo.xoom.lattice.router.RoutableCommand)
   */
  @Override
  public <P,C extends Command,A> void route(final RoutableCommand<P,C,A> command) {
    dispatchCommand(CommandRouter::route, command);
  }
}
