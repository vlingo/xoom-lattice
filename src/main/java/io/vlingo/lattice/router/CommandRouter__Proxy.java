// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.actors.*;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.model.Command;

public class CommandRouter__Proxy implements io.vlingo.lattice.router.CommandRouter {

  private static final String routeRepresentation1 = "route(io.vlingo.lattice.router.RoutableCommand<P, A>)";

  private final Actor actor;
  private final Mailbox mailbox;

  public CommandRouter__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public <P,C extends Command,A> void route(io.vlingo.lattice.router.RoutableCommand<P,C,A> arg0) {
    if (!actor.isStopped()) {
      final SerializableConsumer<CommandRouter> consumer = (actor) -> actor.route(arg0);
      if (mailbox.isPreallocated()) { mailbox.send(actor, CommandRouter.class, consumer, Returns.value(arg0.answer()), routeRepresentation1); }
      else { mailbox.send(new LocalMessage<CommandRouter>(actor, CommandRouter.class, consumer, Returns.value(arg0.answer()), routeRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, routeRepresentation1));
    }
  }
}
