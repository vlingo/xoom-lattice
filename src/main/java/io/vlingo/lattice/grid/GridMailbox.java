package io.vlingo.lattice.grid;

import io.vlingo.actors.*;
import io.vlingo.lattice.grid.application.GridActorControl;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.wire.node.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GridMailbox implements Mailbox {

  private static final Logger log = LoggerFactory.getLogger(GridMailbox.class);

  private final Mailbox local;
  private final Id localId;
  private final Address address;

  private final HashRing<Id> hashRing;

  private final GridActorControl.Outbound outbound;
  private final Consumer<Returns<?>> returnsInterestConusmer;

  public GridMailbox(Mailbox local, Id localId, Address address, HashRing<Id> hashRing, GridActorControl.Outbound outbound, Consumer<Returns<?>> returnsInterestConsumer) {
    this.local = local;
    this.localId = localId;
    this.address = address;
    this.hashRing = hashRing;
    this.outbound = outbound;
    this.returnsInterestConusmer = returnsInterestConsumer;
  }

  private void delegateUnlessIsRemote(Consumer<Id> remote, Runnable consumer) {
    Id nodeOf = hashRing.nodeOf(address.idString());
    if (nodeOf == null || nodeOf.equals(localId)) {
      consumer.run();
    }
    else {
      remote.accept(nodeOf);
    }
  }

  private <R> R delegateUnlessIsRemote(Function<Id, R> remote, Supplier<R> consumer) {
    Id nodeOf = hashRing.nodeOf(address.idString());
    if (nodeOf == null || nodeOf.equals(localId)) {
      return consumer.get();
    }
    else {
      return remote.apply(nodeOf);
    }
  }

  @Override
  public void close() {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::close on: " + nodeOf);
      local.close();
    }, local::close);
  }

  @Override
  public boolean isClosed() {
    return delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::isClosed on: " + nodeOf);
      return local.isClosed();
    }, local::isClosed);
  }

  @Override
  public boolean isDelivering() {
    return delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::isDelivering on: " + nodeOf);
      return local.isDelivering();
    }, local::isDelivering);
  }

  @Override
  public int concurrencyCapacity() {
    return delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::concurrencyCapacity on: " + nodeOf);
      return local.concurrencyCapacity();
    }, local::concurrencyCapacity);
  }

  @Override
  public void resume(String name) {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::resume on: " + nodeOf);
      local.resume(name);
    }, () -> local.resume(name));
  }

  @Override
  public void send(Message message) {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::send(Message) on: " + nodeOf);
      local.send(message);
    }, () -> local.send(message));
  }

  @Override
  public void send(Actor actor, Class<?> protocol, Consumer<?> consumer, Returns<?> returns, String representation) {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::send(Actor, ...) on: " + nodeOf);
      local.send(actor, protocol, consumer, returns, representation);
    }, () -> local.send(actor, protocol, consumer, returns, representation));
  }

  @Override
  public boolean isPreallocated() {
    return delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::isPreallocated on: " + nodeOf);
      return local.isPreallocated();
    }, local::isPreallocated);
  }

  @Override
  public void suspendExceptFor(String name, Class<?>... overrides) {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::suspendExceptFor on: " + nodeOf);
      local.suspendExceptFor(name, overrides);
    }, () -> local.suspendExceptFor(name, overrides));
  }

  @Override
  public boolean isSuspended() {
    return delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::isSuspended on: " + nodeOf);
      return local.isSuspended();
    }, local::isSuspended);
  }

  @Override
  public Message receive() {
    return delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::receive on: " + nodeOf);
      return local.receive();
    }, local::receive);
  }

  @Override
  public int pendingMessages() {
    return delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::pendingMessages on: " + nodeOf);
      return local.pendingMessages();
    }, local::pendingMessages);
  }

  @Override
  public void run() {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::run on: " + nodeOf);
      local.run();
    }, local);
  }
}
