// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.List;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorProxyBase;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.Definition;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Returns;
import io.vlingo.common.Completes;
import io.vlingo.common.SerializableConsumer;

public class MultiConfirming__Proxy extends ActorProxyBase<io.vlingo.lattice.model.projection.MultiConfirming> implements io.vlingo.lattice.model.projection.MultiConfirming {

  private static final String manageConfirmationsForRepresentation1 = "manageConfirmationsFor(io.vlingo.lattice.model.projection.Projectable, int)";
  private static final String managedConfirmationsRepresentation2 = "managedConfirmations()";

  private final Actor actor;
  private final Mailbox mailbox;

  public MultiConfirming__Proxy(final Actor actor, final Mailbox mailbox){
    super(io.vlingo.lattice.model.projection.MultiConfirming.class, Definition.SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public MultiConfirming__Proxy(){
    super();
    this.actor = null;
    this.mailbox = null;
  }

  @Override
  public void manageConfirmationsFor(io.vlingo.lattice.model.projection.Projectable arg0, int arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<MultiConfirming> self = this;
      final SerializableConsumer<MultiConfirming> consumer = (actor) -> actor.manageConfirmationsFor(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      if (mailbox.isPreallocated()) { mailbox.send(actor, MultiConfirming.class, consumer, null, manageConfirmationsForRepresentation1); }
      else { mailbox.send(new LocalMessage<MultiConfirming>(actor, MultiConfirming.class, consumer, manageConfirmationsForRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, manageConfirmationsForRepresentation1));
    }
  }

  @Override
  public Completes<List<Projectable>> managedConfirmations() {
    if (!actor.isStopped()) {
      final SerializableConsumer<MultiConfirming> consumer = (actor) -> actor.managedConfirmations();
      final Completes<List<Projectable>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, MultiConfirming.class, consumer, Returns.value(returnValue), managedConfirmationsRepresentation2); }
      else { mailbox.send(new LocalMessage<MultiConfirming>(actor, MultiConfirming.class, consumer, Returns.value(returnValue), managedConfirmationsRepresentation2)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, managedConfirmationsRepresentation2));
    }
    return null;
  }
}
