package io.vlingo.xoom.lattice.grid.spaces;

import io.vlingo.xoom.actors.*;
import io.vlingo.xoom.actors.Definition.SerializationProxy;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.SerializableConsumer;

import java.util.Optional;

public class DistributedSpace__Proxy extends ActorProxyBase<DistributedSpace> implements DistributedSpace, Proxy {

  private static final String localPutRepresentation1 = "localPut(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Item<T>)";
  private static final String localTakeRepresentation2 = "localTake(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Period)";
  private static final String takeRepresentation3 = "take(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Period)";
  private static final String getRepresentation4 = "get(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Period)";
  private static final String putRepresentation5 = "put(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Item<T>)";
  private static final String itemForRepresentation6 = "itemFor(java.lang.Class<T>, java.lang.Class<? extends io.vlingo.xoom.actors.Actor>, java.lang.Object[])";

  private final Actor actor;
  private final Mailbox mailbox;

  public DistributedSpace__Proxy(final Actor actor, final Mailbox mailbox) {
    super(DistributedSpace.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public DistributedSpace__Proxy() {
    super();
    this.actor = null;
    this.mailbox = null;
  }


  public Address address() {
    return actor.address();
  }

  public boolean equals(final Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (other.getClass() != getClass()) return false;
    return address().equals(Proxy.from(other).address());
  }

  public int hashCode() {
    return 31 + getClass().hashCode() + actor.address().hashCode();
  }

  public String toString() {
    return "DistributedSpace[address=" + actor.address() + "]";
  }


  public <T> Completes<KeyItem<T>> localPut(Key arg0, Item<T> arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<DistributedSpace> self = this;
      final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.localPut(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final Completes<KeyItem<T>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DistributedSpace.class, consumer, Returns.value(returnValue), localPutRepresentation1); }
      else { mailbox.send(new LocalMessage<DistributedSpace>(actor, DistributedSpace.class, consumer, Returns.value(returnValue), localPutRepresentation1)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, localPutRepresentation1));
    }
    return null;
  }

  public <T> Completes<KeyItem<T>> localTake(Key arg0, Period arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<DistributedSpace> self = this;
      final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.localTake(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final Completes<KeyItem<T>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DistributedSpace.class, consumer, Returns.value(returnValue), localTakeRepresentation2); }
      else { mailbox.send(new LocalMessage<DistributedSpace>(actor, DistributedSpace.class, consumer, Returns.value(returnValue), localTakeRepresentation2)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, localTakeRepresentation2));
    }
    return null;
  }

  public <T> Completes<Optional<KeyItem<T>>> take(Key arg0, Period arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<DistributedSpace> self = this;
      final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.take(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final Completes<Optional<KeyItem<T>>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DistributedSpace.class, consumer, Returns.value(returnValue), takeRepresentation3); }
      else { mailbox.send(new LocalMessage<DistributedSpace>(actor, DistributedSpace.class, consumer, Returns.value(returnValue), takeRepresentation3)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, takeRepresentation3));
    }
    return null;
  }

  public <T> Completes<Optional<KeyItem<T>>> get(Key arg0, Period arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<DistributedSpace> self = this;
      final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.get(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final Completes<Optional<KeyItem<T>>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DistributedSpace.class, consumer, Returns.value(returnValue), getRepresentation4); }
      else { mailbox.send(new LocalMessage<DistributedSpace>(actor, DistributedSpace.class, consumer, Returns.value(returnValue), getRepresentation4)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, getRepresentation4));
    }
    return null;
  }

  public <T> Completes<KeyItem<T>> put(Key arg0, Item<T> arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<DistributedSpace> self = this;
      final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.put(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      final Completes<KeyItem<T>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DistributedSpace.class, consumer, Returns.value(returnValue), putRepresentation5); }
      else { mailbox.send(new LocalMessage<DistributedSpace>(actor, DistributedSpace.class, consumer, Returns.value(returnValue), putRepresentation5)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, putRepresentation5));
    }
    return null;
  }

  public <T> Completes<T> itemFor(Class<T> arg0, Class<? extends Actor> arg1, Object[] arg2) {
    if (!actor.isStopped()) {
      ActorProxyBase<DistributedSpace> self = this;
      final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.itemFor(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2));
      final Completes<T> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DistributedSpace.class, consumer, Returns.value(returnValue), itemForRepresentation6); }
      else { mailbox.send(new LocalMessage<DistributedSpace>(actor, DistributedSpace.class, consumer, Returns.value(returnValue), itemForRepresentation6)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, itemForRepresentation6));
    }
    return null;
  }

  public Actor __actor() {
    return actor;
  }
}
