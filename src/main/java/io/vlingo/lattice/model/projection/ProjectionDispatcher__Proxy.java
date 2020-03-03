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

public class ProjectionDispatcher__Proxy extends ActorProxyBase<io.vlingo.lattice.model.projection.ProjectionDispatcher> implements io.vlingo.lattice.model.projection.ProjectionDispatcher {

  private static final String projectToRepresentation1 = "projectTo(io.vlingo.lattice.model.projection.Projection, java.lang.String[])";

  private final Actor actor;
  private final Mailbox mailbox;

  public ProjectionDispatcher__Proxy(final Actor actor, final Mailbox mailbox){
    super(io.vlingo.lattice.model.projection.ProjectionDispatcher.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public ProjectionDispatcher__Proxy(){
    super();
    this.actor = null;
    this.mailbox = null;
  }

  @Override
  public void projectTo(io.vlingo.lattice.model.projection.Projection arg0, java.lang.String[] arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<ProjectionDispatcher> self = this;
      final SerializableConsumer<ProjectionDispatcher> consumer = (actor) -> actor.projectTo(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      if (mailbox.isPreallocated()) { mailbox.send(actor, ProjectionDispatcher.class, consumer, null, projectToRepresentation1); }
      else { mailbox.send(new LocalMessage<ProjectionDispatcher>(actor, ProjectionDispatcher.class, consumer, projectToRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, projectToRepresentation1));
    }
  }
}
