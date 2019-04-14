// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.actors.Stage;

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

  <P,A> void route(final RoutableCommand<P,A> command);

  enum Type { LoadBalancing, Partitioning, RoundRobin };
}
