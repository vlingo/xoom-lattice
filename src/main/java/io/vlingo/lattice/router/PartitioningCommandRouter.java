// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.actors.ContentBasedRouter;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Routee;
import io.vlingo.actors.RouterSpecification;
import io.vlingo.actors.Routing;
import io.vlingo.lattice.model.Command;

/**
 * The {@code CommandRouter} implementation for partitioning on the {@code RoutableCommand}.
 */
public class PartitioningCommandRouter extends ContentBasedRouter<CommandRouter> implements CommandRouter {
  private Routee<CommandRouter> currentRoutee;
  private final int totalRoutees;

  /**
   * Constructs my default state.
   * @param totalRoutees the int number or routees to create
   */
  public PartitioningCommandRouter(final int totalRoutees) {
    super(new RouterSpecification<CommandRouter>(
            totalRoutees,
            Definition.has(CommandRouterWorkerActor.class, Definition.NoParameters),
            CommandRouter.class));

    this.totalRoutees = totalRoutees;
  }

  /**
   * Route to routee of calculated partition.
   * @see io.vlingo.lattice.router.CommandRouter#route(io.vlingo.lattice.router.RoutableCommand)
   */
  @Override
  public <P,C extends Command,A> void route(final RoutableCommand<P,C,A> command) {
    final int partition = command.hashCode() % totalRoutees;
    currentRoutee = routeeAt(partition);
    dispatchCommand(CommandRouter::route, command);
  }

  /**
   * @see io.vlingo.actors.ContentBasedRouter#computeRouting()
   */
  @Override
  protected Routing<CommandRouter> computeRouting() {
    return Routing.with(currentRoutee);
  }
}
