package io.vlingo.lattice.grid;

import io.vlingo.actors.*;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.node.Id;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GridMailbox implements Mailbox {

  private final Mailbox local;
  private final Id localId;
  private final Address address;

  private final HashRing<Id> hashRing;

  private final ApplicationOutboundStream outbound;
  private final Consumer<Returns<?>> returnsInterestConusmer;

  public GridMailbox(Mailbox local, Id localId, Address address, HashRing<Id> hashRing, ApplicationOutboundStream outbound, Consumer<Returns<?>> returnsInterestConsumer) {
    this.local = local;
    this.localId = localId;
    this.address = address;
    this.hashRing = hashRing;
    this.outbound = outbound;
    this.returnsInterestConusmer = returnsInterestConsumer;
  }

  private void delegateUnlessIsRemote(Consumer<Id> remote, Runnable consumer) {
    Id nodeOf = hashRing.nodeOf(address.idString());
    if (nodeOf.equals(localId)) {
      consumer.run();
    }
    else {
      remote.accept(nodeOf);
    }
  }

  private <R> R delegateUnlessIsRemote(Function<Id, R> remote, Supplier<R> consumer) {
    Id nodeOf = hashRing.nodeOf(address.idString());
    if (nodeOf.equals(localId)) {
      return consumer.get();
    }
    else {
      return remote.apply(nodeOf);
    }
  }

  @Override
  public void close() {
    delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::close on: " + nodeOf);
      local.close();
    }, local::close);
  }

  @Override
  public boolean isClosed() {
    return delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::isClosed on: " + nodeOf);
      return local.isClosed();
    }, local::isClosed);
  }

  @Override
  public boolean isDelivering() {
    return delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::isDelivering on: " + nodeOf);
      return local.isDelivering();
    }, local::isDelivering);
  }

  @Override
  public int concurrencyCapacity() {
    return delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::concurrencyCapacity on: " + nodeOf);
      return local.concurrencyCapacity();
    }, local::concurrencyCapacity);
  }

  @Override
  public void resume(String name) {
    delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::resume on: " + nodeOf);
      local.resume(name);
    }, () -> local.resume(name));
  }

  @Override
  public void send(Message message) {
    delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::send(Message) on: " + nodeOf);
      local.send(message);
    }, () -> local.send(message));
  }

  @Override
  public void send(Actor actor, Class<?> protocol, Consumer<?> consumer, Returns<?> returns, String representation) {
    delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::send(Actor, ...) on: " + nodeOf);
      local.send(actor, protocol, consumer, returns, representation);
    }, () -> local.send(actor, protocol, consumer, returns, representation));
  }

  @Override
  public boolean isPreallocated() {
    return delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::isPreallocated on: " + nodeOf);
      return local.isPreallocated();
    }, local::isPreallocated);
  }

  @Override
  public void suspendExceptFor(String name, Class<?>... overrides) {
    delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::suspendExceptFor on: " + nodeOf);
      local.suspendExceptFor(name, overrides);
    }, () -> local.suspendExceptFor(name, overrides));
  }

  @Override
  public boolean isSuspended() {
    return delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::isSuspended on: " + nodeOf);
      return local.isSuspended();
    }, local::isSuspended);
  }

  @Override
  public Message receive() {
    return delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::receive on: " + nodeOf);
      return local.receive();
    }, local::receive);
  }

  @Override
  public int pendingMessages() {
    return delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::pendingMessages on: " + nodeOf);
      return local.pendingMessages();
    }, local::pendingMessages);
  }

  @Override
  public void run() {
    delegateUnlessIsRemote(nodeOf -> {
      System.out.println("Remote::run on: " + nodeOf);
      local.run();
    }, local);
  }
}
