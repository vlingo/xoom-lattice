// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorProxyBase;
import io.vlingo.xoom.actors.DeadLetter;
import io.vlingo.xoom.actors.Definition.SerializationProxy;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.SerializableConsumer;

public class Space__Proxy extends ActorProxyBase<io.vlingo.xoom.lattice.grid.spaces.Space> implements io.vlingo.xoom.lattice.grid.spaces.Space {

  private static final String getRepresentation1 = "get(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Period)";
  private static final String putRepresentation2 = "put(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Item<T>)";
  private static final String takeRepresentation3 = "take(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Period)";
  private static final String itemForRepresentation4 = "itemFor(java.lang.Class<T>, java.lang.Class<? extends io.vlingo.xoom.actors.Actor>, java.lang.Object[])";

  private final Actor actor;
  private final Mailbox mailbox;

  public Space__Proxy(final Actor actor, final Mailbox mailbox){
    super(io.vlingo.xoom.lattice.grid.spaces.Space.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public Space__Proxy(){
    super();
    this.actor = null;
    this.mailbox = null;
  }

  @Override
  public <T>io.vlingo.xoom.common.Completes<java.util.Optional<io.vlingo.xoom.lattice.grid.spaces.KeyItem<T>>> get(io.vlingo.xoom.lattice.grid.spaces.Key arg0, io.vlingo.xoom.lattice.grid.spaces.Period arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<Space> self = this;
      final SerializableConsumer<Space> consumer = (actor) -> actor.get(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final io.vlingo.xoom.common.Completes<java.util.Optional<io.vlingo.xoom.lattice.grid.spaces.KeyItem<T>>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Space.class, consumer, Returns.value(returnValue), getRepresentation1); }
      else { mailbox.send(new LocalMessage<Space>(actor, Space.class, consumer, Returns.value(returnValue), getRepresentation1)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, getRepresentation1));
    }
    return null;
  }
  @Override
  public <T>io.vlingo.xoom.common.Completes<io.vlingo.xoom.lattice.grid.spaces.KeyItem<T>> put(io.vlingo.xoom.lattice.grid.spaces.Key arg0, io.vlingo.xoom.lattice.grid.spaces.Item<T> arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<Space> self = this;
      final SerializableConsumer<Space> consumer = (actor) -> actor.put(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final io.vlingo.xoom.common.Completes<io.vlingo.xoom.lattice.grid.spaces.KeyItem<T>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Space.class, consumer, Returns.value(returnValue), putRepresentation2); }
      else { mailbox.send(new LocalMessage<Space>(actor, Space.class, consumer, Returns.value(returnValue), putRepresentation2)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, putRepresentation2));
    }
    return null;
  }
  @Override
  public <T>io.vlingo.xoom.common.Completes<java.util.Optional<io.vlingo.xoom.lattice.grid.spaces.KeyItem<T>>> take(io.vlingo.xoom.lattice.grid.spaces.Key arg0, io.vlingo.xoom.lattice.grid.spaces.Period arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<Space> self = this;
      final SerializableConsumer<Space> consumer = (actor) -> actor.take(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final io.vlingo.xoom.common.Completes<java.util.Optional<io.vlingo.xoom.lattice.grid.spaces.KeyItem<T>>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Space.class, consumer, Returns.value(returnValue), takeRepresentation3); }
      else { mailbox.send(new LocalMessage<Space>(actor, Space.class, consumer, Returns.value(returnValue), takeRepresentation3)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, takeRepresentation3));
    }
    return null;
  }
  @Override
  public <T>io.vlingo.xoom.common.Completes<T> itemFor(java.lang.Class<T> arg0, java.lang.Class<? extends io.vlingo.xoom.actors.Actor> arg1, java.lang.Object... arg2) {
    if (!actor.isStopped()) {
      ActorProxyBase<Space> self = this;
      final SerializableConsumer<Space> consumer = (actor) -> actor.itemFor(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2));
      final io.vlingo.xoom.common.Completes<T> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Space.class, consumer, Returns.value(returnValue), itemForRepresentation4); }
      else { mailbox.send(new LocalMessage<Space>(actor, Space.class, consumer, Returns.value(returnValue), itemForRepresentation4)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, itemForRepresentation4));
    }
    return null;
  }
}
