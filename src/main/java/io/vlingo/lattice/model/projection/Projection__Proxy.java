// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorProxyBase;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.Definition.SerializationProxy;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.common.SerializableConsumer;

public class Projection__Proxy extends ActorProxyBase<io.vlingo.lattice.model.projection.Projection> implements io.vlingo.lattice.model.projection.Projection {

  private static final String projectWithRepresentation1 = "projectWith(io.vlingo.lattice.model.projection.Projectable, io.vlingo.lattice.model.projection.ProjectionControl)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Projection__Proxy(final Actor actor, final Mailbox mailbox){
    super(io.vlingo.lattice.model.projection.Projection.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public Projection__Proxy(){
    super();
    this.actor = null;
    this.mailbox = null;
  }

  @Override
  public void projectWith(io.vlingo.lattice.model.projection.Projectable arg0, io.vlingo.lattice.model.projection.ProjectionControl arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<Projection> self = this;
      final SerializableConsumer<Projection> consumer = (actor) -> actor.projectWith(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Projection.class, consumer, null, projectWithRepresentation1); }
      else { mailbox.send(new LocalMessage<Projection>(actor, Projection.class, consumer, projectWithRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, projectWithRepresentation1));
    }
  }
}
