package io.vlingo.lattice.model.projection;

import io.vlingo.actors.Actor;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.common.SerializableConsumer;

public class ProjectionDispatcher__Proxy implements io.vlingo.lattice.model.projection.ProjectionDispatcher {

  private static final String projectToRepresentation1 = "projectTo(io.vlingo.lattice.model.projection.Projection, java.lang.String)";

  private final Actor actor;
  private final Mailbox mailbox;

  public ProjectionDispatcher__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public void projectTo(io.vlingo.lattice.model.projection.Projection arg0, java.lang.String[] arg1) {
    if (!actor.isStopped()) {
      final SerializableConsumer<ProjectionDispatcher> consumer = (actor) -> actor.projectTo(arg0, arg1);
      if (mailbox.isPreallocated()) { mailbox.send(actor, ProjectionDispatcher.class, consumer, null, projectToRepresentation1); }
      else { mailbox.send(new LocalMessage<ProjectionDispatcher>(actor, ProjectionDispatcher.class, consumer, projectToRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, projectToRepresentation1));
    }
  }
}
