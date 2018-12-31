package io.vlingo.lattice.model.projection;

import io.vlingo.actors.Actor;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

public class Projection__Proxy implements io.vlingo.lattice.model.projection.Projection {

  private static final String projectWithRepresentation1 = "projectWith(io.vlingo.lattice.model.projection.Projectable, io.vlingo.lattice.model.projection.ProjectionControl)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Projection__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public void projectWith(io.vlingo.lattice.model.projection.Projectable arg0, io.vlingo.lattice.model.projection.ProjectionControl arg1) {
    if (!actor.isStopped()) {
      final java.util.function.Consumer<Projection> consumer = (actor) -> actor.projectWith(arg0, arg1);
      if (mailbox.isPreallocated()) { mailbox.send(actor, Projection.class, consumer, null, projectWithRepresentation1); }
      else { mailbox.send(new LocalMessage<Projection>(actor, Projection.class, consumer, projectWithRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, projectWithRepresentation1));
    }
  }
}
