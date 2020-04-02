// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.vlingo.common.identity.IdentityGeneratorType;
import io.vlingo.lattice.grid.GridNode;
import io.vlingo.lattice.grid.GridNodeBootstrap;
import io.vlingo.lattice.grid.application.OutboundGridActorControl;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.lattice.grid.hashring.MurmurSortedMapHashRing;
import io.vlingo.wire.node.Id;

public class Grid extends Stage implements GridRuntime {

  private static final String INSTANCE_NAME = UUID.randomUUID().toString();

  private final GridNodeBootstrap gridNodeBootstrap;
  private final HashRing<Id> hashRing;

  private Id nodeId;
  private OutboundGridActorControl outbound;

  public static Grid instance(World world) {
    return world.resolveDynamic(INSTANCE_NAME, Grid.class);
  }

  public static Grid start(final String worldName, final String gridNodeName) throws Exception {
    return start(worldName, Configuration.define(), io.vlingo.cluster.model.Properties.open(), gridNodeName);
  }

  public static Grid start(final World world, final String gridNodeName) throws Exception {
    return start(world, new GridAddressFactory(IdentityGeneratorType.RANDOM), io.vlingo.cluster.model.Properties.open(), gridNodeName);
  }

  public static Grid start(final String worldName, final Configuration worldConfiguration, final String gridNodeName) throws Exception {
    return start(worldName, worldConfiguration, io.vlingo.cluster.model.Properties.open(), gridNodeName);
  }

  public static Grid start(final String worldName, final Configuration worldConfiguration, final io.vlingo.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    return start(worldName, new GridAddressFactory(IdentityGeneratorType.RANDOM), worldConfiguration, clusterProperties, gridNodeName);
  }

  public static Grid start(final World world, final io.vlingo.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    return start(world, new GridAddressFactory(IdentityGeneratorType.RANDOM), clusterProperties, gridNodeName);
  }

  public static Grid start(final String worldName, final AddressFactory addressFactory, final Configuration worldConfiguration, final io.vlingo.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    final World world = World.start(worldName, worldConfiguration);
    final Grid grid = new Grid(world, addressFactory, clusterProperties, gridNodeName);
    return grid;
  }

  public static Grid start(final World world, final AddressFactory addressFactory, final io.vlingo.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    return new Grid(world, addressFactory, clusterProperties, gridNodeName);
  }

  public Grid(final World world, final AddressFactory addressFactory, final io.vlingo.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    super(world, addressFactory, gridNodeName);
    this.hashRing = new MurmurSortedMapHashRing<>(100);
    extenderStartDirectoryScanner();
    this.gridNodeBootstrap = GridNodeBootstrap.boot(this, gridNodeName, clusterProperties, false);
    world.registerDynamic(INSTANCE_NAME, this);
  }

  @Override
  protected ActorFactory.MailboxWrapper mailboxWrapper() {
    return (address, mailbox) ->
        new GridMailbox(mailbox, nodeId,
            address, hashRing, outbound);
  }

  public void terminate() {
    world().terminate();
  }

  //====================================
  // GridRuntime
  //====================================

  /**
   * Answers the Actor at the specified Address.
   *
   * @param address the Address of the actor
   * @return the Actor
   */
  @Override
  public Actor actorAt(Address address) {
    return directory.actorOf(address);
  }

  @Override
  <T> T actorThunkFor(Class<T> protocol, Definition definition, Address address) {
    final Mailbox actorMailbox = this.allocateMailbox(definition, address, null);
    actorMailbox.suspendExceptFor(GridActor.Resume, RelocationSnapshotConsumer.class);
    final ActorProtocolActor<T> actor =
        actorProtocolFor(
            protocol,
            definition,
            definition.parentOr(world.defaultParent()),
            address,
            actorMailbox,
            definition.supervisor(),
            definition.loggerOr(world.defaultLogger()));

    return actor.protocolActor();
  }

  @Override
  public GridNodeBootstrap gridNodeBootstrap() {
    return gridNodeBootstrap;
  }

  @Override
  public HashRing<Id> hashRing() {
    return hashRing;
  }

  @Override
  public void nodeJoined(final Id newNode) {
    if (nodeId.equals(newNode)) {
      // self is added to the hash-ring on GridNode#start
      return;
    }

    final HashRing<Id> copy = this.hashRing.copy();
    this.hashRing.includeNode(newNode);

    directory.addresses().stream()
        .filter(shouldRelocateTo(copy, newNode))
        .forEach(address -> {
          final GridActor<?> actor = ((GridActor<?>) directory.actorOf(address));
          if (!actor.isSuspendedForRelocation()) {
            actor.suspendForRelocation();
            outbound.relocate(
                newNode, nodeId, Definition.SerializationProxy.from(actor.definition()),
                address, actor.provideRelocationSnapshot(), actor.pending());
          }
        });
  }

  private Predicate<Address>  shouldRelocateTo(HashRing<Id> previous, Id newNode) {
    return address -> address.isDistributable()
        && isAssignedTo(previous, address, nodeId)
        && isAssignedTo(this.hashRing, address, newNode);
  }

  private static boolean isAssignedTo(HashRing<Id> ring, Address address, Id node) {
    return node.equals(ring.nodeOf(address.idString()));
  }

  @Override
  public void nodeLeft(final Id removedNode) {
    if (nodeId.equals(removedNode)) {
      // we don't want to remove ourselves
      return;
    }

    final HashRing<Id> copy = this.hashRing.copy();
    this.hashRing.excludeNode(removedNode);

    directory.addresses().stream()
        .filter(shouldRecoverFrom(copy, removedNode))
        .forEach(address -> {
          final GridActor<?> actor = (GridActor<?>) directory.actorOf(address);
          actor.resumeFromRelocation();
        });
  }

  private Predicate<Address> shouldRecoverFrom(HashRing<Id> previous, Id removedNode) {
    return address -> address.isDistributable()
        &&isAssignedTo(previous, address, removedNode)
        && isAssignedTo(hashRing, address, nodeId);
  }

  @Override
  public void setOutbound(final OutboundGridActorControl outbound) {
    this.outbound = outbound;
  }

  @Override
  public void setNodeId(final Id nodeId) {
    this.nodeId = nodeId;
  }

  public ClassLoader worldLoader() {
    return world().classLoader();
  }

  //====================================
  // Internal implementation
  //====================================

  @Override
  protected <T> ActorProtocolActor<T> actorProtocolFor(Class<T> protocol, Definition definition, Actor parent, Address maybeAddress, Mailbox maybeMailbox, Supervisor maybeSupervisor, Logger logger) {
    final Address address = maybeAddress == null ? addressFactory().unique() : maybeAddress;
    final Id node = hashRing.nodeOf(address.idString());
    final Mailbox mailbox = maybeRemoteMailbox(address, definition, maybeMailbox, () -> {
      outbound.start(node, nodeId, protocol, address, Definition.SerializationProxy.from(definition));
    }, id -> {
      outbound.standby(id, nodeId, protocol, Definition.SerializationProxy.from(definition), address);
    });
    return super.actorProtocolFor(protocol, definition, parent, address, mailbox, maybeSupervisor, logger);
  }

  @Override
  protected ActorProtocolActor<Object>[] actorProtocolFor(Class<?>[] protocols, Definition definition, Actor parent, Address maybeAddress, Mailbox maybeMailbox, Supervisor maybeSupervisor, Logger logger) {
    final Address address = maybeAddress == null ? addressFactory().unique() : maybeAddress;
    final Id node = hashRing.nodeOf(address.idString());
    final Mailbox mailbox = maybeRemoteMailbox(address, definition, maybeMailbox, () -> {
      outbound.start(node, nodeId, protocols[0], address, Definition.SerializationProxy.from(definition)); // TODO remote start all protocols
    }, id -> {
      outbound.standby(id, nodeId, protocols[0], Definition.SerializationProxy.from(definition), address);
    });
    return super.actorProtocolFor(protocols, definition, parent, address, mailbox, maybeSupervisor, logger);
  }

  private Mailbox maybeRemoteMailbox(final Address address,
                                     final Definition definition,
                                     final Mailbox maybeMailbox,
                                     final Runnable outStart,
                                     final Consumer<Id> outStandby) {
    final List<Id> nodes = hashRing.nodesOf(address.idString());
    final Mailbox __mailbox;
    if (!nodes.isEmpty() && !nodes.get(0).equals(nodeId)) {
      outStart.run();
      __mailbox = allocateMailbox(definition, address, maybeMailbox);
      if (!__mailbox.isSuspendedFor(GridActor.Resume)) {
        __mailbox.suspendExceptFor(GridActor.Resume, RelocationSnapshotConsumer.class);
      }
    }
    else {
      __mailbox = maybeMailbox;
    }
    return __mailbox;
  }
}
