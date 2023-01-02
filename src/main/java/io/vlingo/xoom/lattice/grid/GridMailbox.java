// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Message;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.Stoppable;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.application.GridActorControl;
import io.vlingo.xoom.lattice.grid.hashring.HashRing;
import io.vlingo.xoom.wire.node.Id;

public class GridMailbox implements Mailbox {

  private static final Logger log = LoggerFactory.getLogger(GridMailbox.class);

  private final Mailbox local;
  private final Id localId;
  private final Address address;

  private final HashRing<Id> hashRing;

  private final GridActorControl.Outbound outbound;

  public GridMailbox(Mailbox local, Id localId, Address address, HashRing<Id> hashRing, GridActorControl.Outbound outbound) {
    this.local = local;
    this.localId = localId;
    this.address = address;
    this.hashRing = hashRing;
    this.outbound = outbound;
  }

  private void delegateUnlessIsRemote(Consumer<Id> remote, Runnable consumer) {
    if (!address.isDistributable()) {
      consumer.run();
      return;
    }
    Id nodeOf = hashRing.nodeOf(address.idString());
    if (nodeOf == null || nodeOf.equals(localId)) {
      consumer.run();
    } else {
      remote.accept(nodeOf);
    }
  }

  private <R> R delegateUnlessIsRemote(Function<Id, R> remote, Supplier<R> supplier) {
    if (!address.isDistributable()) {
      return supplier.get();
    }
    Id nodeOf = hashRing.nodeOf(address.idString());
    if (nodeOf == null || nodeOf.equals(localId)) {
      return supplier.get();
    } else {
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

  @SuppressWarnings("serial")
  private static final Set<Class<?>> overrides = new HashSet<Class<?>>() {{
    add(Stoppable.class);
  }};

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void send(Message message) {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::send(Message) on: " + nodeOf);
      LocalMessage localMessage = (LocalMessage) message; // TODO make this work with Message ?
      if (overrides.contains(localMessage.protocol())) {
        local.send(message);
      }
      outbound.gridDeliver(
          nodeOf, localId, localMessage.returns(), message.protocol(),
          address, Definition.SerializationProxy.from(message.actor().definition()),
          localMessage.consumer(), message.representation());
    }, () -> local.send(message));
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public void send(Actor actor, Class<?> protocol, SerializableConsumer<?> consumer, Returns<?> returns, String representation) {
    delegateUnlessIsRemote(nodeOf -> {
      log.debug("Remote::send(Actor, ...) on: " + nodeOf);
      if (overrides.contains(protocol)) {
        local.send(actor, protocol, consumer, returns, representation);
      }
      outbound.gridDeliver(nodeOf, localId, returns, (Class<Object>) protocol,
          address, Definition.SerializationProxy.from(actor.definition()),
          (SerializableConsumer<Object>) consumer, representation);
    }, () -> local.send(actor, protocol, consumer, returns, representation));
  }

  @Override
  public boolean isPreallocated() {
    return local.isPreallocated();
  }

  @Override
  public void suspendExceptFor(String name, Class<?>... overrides) {
    local.suspendExceptFor(name, overrides);
  }

  @Override
  public boolean isSuspended() {
    return delegateUnlessIsRemote(nodeOf -> false, local::isSuspended);
  }

  @Override
  public boolean isSuspendedFor(String name) {
    return local.isSuspendedFor(name);
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
