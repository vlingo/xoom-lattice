// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.router;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorProxyBase;
import io.vlingo.xoom.actors.DeadLetter;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.model.Command;

public class CommandRouter__Proxy extends ActorProxyBase<CommandRouter> implements io.vlingo.xoom.lattice.router.CommandRouter {

  private static final long serialVersionUID = -1485671202542239243L;

  private static final String routeRepresentation1 = "route(io.vlingo.xoom.lattice.router.RoutableCommand<P, A>)";

  private Actor actor;
  private Mailbox mailbox;

  public CommandRouter__Proxy(final Actor actor, final Mailbox mailbox){
    super(CommandRouter.class, Definition.SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public CommandRouter__Proxy() { }

  @Override
  public <P,C extends Command,A> void route(io.vlingo.xoom.lattice.router.RoutableCommand<P,C,A> arg0) {
    if (!actor.isStopped()) {
      ActorProxyBase<CommandRouter> self = this;
      final SerializableConsumer<CommandRouter> consumer = (a) -> a.route(ActorProxyBase.thunk(self, (Actor)a, arg0));
      if (mailbox.isPreallocated()) { mailbox.send(actor, CommandRouter.class, consumer, Returns.value(arg0.answer()), routeRepresentation1); }
      else { mailbox.send(new LocalMessage<CommandRouter>(actor, CommandRouter.class, consumer, Returns.value(arg0.answer()), routeRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, routeRepresentation1));
    }
  }
}
