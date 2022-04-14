// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorProxyBase;
import io.vlingo.xoom.actors.DeadLetter;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.common.SerializableConsumer;

public class ProjectionControl__Proxy extends ActorProxyBase<ProjectionControl> implements io.vlingo.xoom.lattice.model.projection.ProjectionControl {

  private static final String confirmProjectedRepresentation1 = "confirmProjected(java.lang.String)";

  private final Actor actor;
  private final Mailbox mailbox;

  public ProjectionControl__Proxy(final Actor actor, final Mailbox mailbox){
    super(io.vlingo.xoom.lattice.model.projection.ProjectionControl.class, Definition.SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public ProjectionControl__Proxy(){
    super();
    this.actor = null;
    this.mailbox = null;
  }

  @Override
  public void confirmProjected(java.lang.String arg0) {
    if (!actor.isStopped()) {
      ActorProxyBase<ProjectionControl> self = this;
      final SerializableConsumer<ProjectionControl> consumer = (actor) -> actor.confirmProjected(ActorProxyBase.thunk(self, (Actor)actor, arg0));
      if (mailbox.isPreallocated()) { mailbox.send(actor, ProjectionControl.class, consumer, null, confirmProjectedRepresentation1); }
      else { mailbox.send(new LocalMessage<ProjectionControl>(actor, ProjectionControl.class, consumer, confirmProjectedRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, confirmProjectedRepresentation1));
    }
  }
}
