// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.router;

import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.lattice.model.Command;

public interface CommandRouter {
  static CommandRouter of(final Stage stage, final Type type, final int totalRoutees) {
    switch (type) {
    case LoadBalancing:
      return stage.actorFor(CommandRouter.class, LoadBalancingCommandRouter.class, totalRoutees);
    case Partitioning:
      return stage.actorFor(CommandRouter.class, PartitioningCommandRouter.class, totalRoutees);
    case RoundRobin:
      return stage.actorFor(CommandRouter.class, RoundRobinCommandRouter.class, totalRoutees);
    }
    throw new IllegalArgumentException("The command router type is not mapped: " + type);
  }

  <P,C extends Command,A> void route(final RoutableCommand<P,C,A> command);

  enum Type { LoadBalancing, Partitioning, RoundRobin };
}
