// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import io.vlingo.actors.Actor;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Returns;
import io.vlingo.common.Completes;
import io.vlingo.common.SerializableConsumer;

public class Entity__Proxy implements io.vlingo.lattice.model.sourcing.Entity {

  private static final String doTest1Representation1 = "doTest1()";
  private static final String doTest2Representation2 = "doTest2()";
  private static final String doTest3Representation3 = "doTest3()";

  private final Actor actor;
  private final Mailbox mailbox;

  public Entity__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void doTest1() {
    if (!actor.isStopped()) {
      final SerializableConsumer<Entity> consumer = (actor) -> actor.doTest1();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Entity.class, consumer, null, doTest1Representation1); }
      else { mailbox.send(new LocalMessage<Entity>(actor, Entity.class, consumer, doTest1Representation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, doTest1Representation1));
    }
  }
  @Override
  public void doTest2() {
    if (!actor.isStopped()) {
      final SerializableConsumer<Entity> consumer = (actor) -> actor.doTest2();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Entity.class, consumer, null, doTest2Representation2); }
      else { mailbox.send(new LocalMessage<Entity>(actor, Entity.class, consumer, doTest2Representation2)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, doTest2Representation2));
    }
  }
  @Override
  public io.vlingo.common.Completes<java.lang.String> doTest3() {
    if (!actor.isStopped()) {
      final SerializableConsumer<Entity> consumer = (actor) -> actor.doTest3();
      final io.vlingo.common.Completes<java.lang.String> completes = Completes.using(actor.completesId(), actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Entity.class, consumer, Returns.value(completes), doTest3Representation3); }
      else { mailbox.send(new LocalMessage<Entity>(actor, Entity.class, consumer, Returns.value(completes), doTest3Representation3)); }
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, doTest3Representation3));
    }
    return null;
  }
}
