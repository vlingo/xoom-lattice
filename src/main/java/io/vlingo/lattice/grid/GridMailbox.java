package io.vlingo.lattice.grid;

import io.vlingo.actors.*;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.node.Id;

import java.util.function.Consumer;
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

  private void delegateUnlessIsRemote(Runnable remote, Runnable consumer) {
    if (hashRing.nodeOf(address.idString()).equals(localId)) {
      consumer.run();
    }
    else {
      remote.run();
    }
  }

  private <R> R delegateUnlessIsRemote(Supplier<R> remote, Supplier<R> consumer) {
    if (hashRing.nodeOf(address.idString()).equals(localId)) {
      return consumer.get();
    }
    else {
      return remote.get();
    }
  }

  @Override
  public void close() {
    delegateUnlessIsRemote(() -> {
      System.out.println("Remote::close");
      local.close();
    }, local::close);
  }

  @Override
  public boolean isClosed() {
    return delegateUnlessIsRemote(() -> {
      System.out.println("Remote::isClosed");
      return local.isClosed();
    }, local::isClosed);
  }

  @Override
  public boolean isDelivering() {
    return delegateUnlessIsRemote(() -> {
      System.out.println("Remote::isDelivering");
      return local.isDelivering();
    }, local::isDelivering);
  }

  @Override
  public int concurrencyCapacity() {
    return delegateUnlessIsRemote(() -> {
      System.out.println("Remote::concurrencyCapacity");
      return local.concurrencyCapacity();
    }, local::concurrencyCapacity);
  }

  @Override
  public void resume(String name) {
    delegateUnlessIsRemote(() -> {
      System.out.println("Remote::resume");
      local.resume(name);
    }, () -> local.resume(name));
  }

  @Override
  public void send(Message message) {
    delegateUnlessIsRemote(() -> {
      System.out.println("Remote::send(Message)");
      local.send(message);
    }, () -> local.send(message));
  }

  @Override
  public void send(Actor actor, Class<?> protocol, Consumer<?> consumer, Returns<?> returns, String representation) {
    delegateUnlessIsRemote(() -> {
      System.out.println("Remote::send(Actor, ...)");
      local.send(actor, protocol, consumer, returns, representation);
    }, () -> local.send(actor, protocol, consumer, returns, representation));
  }

  @Override
  public boolean isPreallocated() {
    return delegateUnlessIsRemote(() -> {
      System.out.println("Remote::isPreallocated");
      return local.isPreallocated();
    }, local::isPreallocated);
  }

  @Override
  public void suspendExceptFor(String name, Class<?>... overrides) {
    delegateUnlessIsRemote(() -> {
      System.out.println("Remote::suspendExceptFor");
      local.suspendExceptFor(name, overrides);
    }, () -> local.suspendExceptFor(name, overrides));
  }

  @Override
  public boolean isSuspended() {
    return delegateUnlessIsRemote(() -> {
      System.out.println("Remote::isSuspended");
      return local.isSuspended();
    }, local::isSuspended);
  }

  @Override
  public Message receive() {
    return delegateUnlessIsRemote(() -> {
      System.out.println("Remote::receive");
      return local.receive();
    }, local::receive);
  }

  @Override
  public int pendingMessages() {
    return delegateUnlessIsRemote(() -> {
      System.out.println("Remote::pendingMessages");
      return local.pendingMessages();
    }, local::pendingMessages);
  }

  @Override
  public void run() {
    delegateUnlessIsRemote(() -> {
      System.out.println("Remote::run");
      local.run();
    }, local);
  }
}
