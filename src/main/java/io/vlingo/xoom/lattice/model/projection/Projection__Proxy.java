// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.DeadLetter;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.common.SerializableConsumer;

public class Projection__Proxy implements io.vlingo.xoom.lattice.model.projection.Projection {

  private static final String projectWithRepresentation1 = "projectWith(io.vlingo.xoom.lattice.model.projection.Projectable, io.vlingo.xoom.lattice.model.projection.ProjectionControl)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Projection__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public void projectWith(io.vlingo.xoom.lattice.model.projection.Projectable arg0, io.vlingo.xoom.lattice.model.projection.ProjectionControl arg1) {
    if (!actor.isStopped()) {
      final SerializableConsumer<Projection> consumer = (actor) -> actor.projectWith(arg0, arg1);
      if (mailbox.isPreallocated()) { mailbox.send(actor, Projection.class, consumer, null, projectWithRepresentation1); }
      else { mailbox.send(new LocalMessage<Projection>(actor, Projection.class, consumer, projectWithRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, projectWithRepresentation1));
    }
  }
}
