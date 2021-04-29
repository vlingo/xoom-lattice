// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.UUID;

import org.slf4j.LoggerFactory;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorFactory;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.AddressFactory;
import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Relocatable;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.Supervisor;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.__InternalOnlyAccessor;
import io.vlingo.xoom.common.identity.IdentityGeneratorType;
import io.vlingo.xoom.lattice.grid.application.GridActorControl;
import io.vlingo.xoom.lattice.grid.application.QuorumObserver;
import io.vlingo.xoom.lattice.grid.hashring.HashRing;
import io.vlingo.xoom.lattice.grid.hashring.MurmurSortedMapHashRing;
import io.vlingo.xoom.wire.node.Id;

public class Grid extends Stage implements GridRuntime {
  private static final int GridStageBuckets = 32;
  private static final int GridStageInitialCapacity = 16_384;

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Grid.class);

  private static final String INSTANCE_NAME = UUID.randomUUID().toString();

  public static Grid instance(World world) {
    return world.resolveDynamic(INSTANCE_NAME, Grid.class);
  }

  public static Grid start(final String worldName, final String gridNodeName) throws Exception {
    return start(worldName, Configuration.define(), io.vlingo.xoom.cluster.model.Properties.open(), gridNodeName);
  }

  public static Grid start(final World world, final String gridNodeName) throws Exception {
    return start(world, new GridAddressFactory(IdentityGeneratorType.RANDOM), io.vlingo.xoom.cluster.model.Properties.open(), gridNodeName);
  }

  public static Grid start(final String worldName, final Configuration worldConfiguration, final String gridNodeName) throws Exception {
    return start(worldName, worldConfiguration, io.vlingo.xoom.cluster.model.Properties.open(), gridNodeName);
  }

  public static Grid start(final String worldName, final Configuration worldConfiguration, final io.vlingo.xoom.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    return start(worldName, new GridAddressFactory(IdentityGeneratorType.RANDOM), worldConfiguration, clusterProperties, gridNodeName);
  }

  public static Grid start(final World world, final io.vlingo.xoom.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    return start(world, new GridAddressFactory(IdentityGeneratorType.RANDOM), clusterProperties, gridNodeName);
  }

  public static Grid start(final String worldName, final AddressFactory addressFactory, final Configuration worldConfiguration, final io.vlingo.xoom.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    final World world = World.start(worldName, worldConfiguration);
    return new Grid(world, addressFactory, clusterProperties, gridNodeName);
  }

  public static Grid start(final World world, final AddressFactory addressFactory, final io.vlingo.xoom.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    return new Grid(world, addressFactory, clusterProperties, gridNodeName);
  }


  private final GridNodeBootstrap gridNodeBootstrap;
  private final HashRing<Id> hashRing;

  private Id nodeId;
  private GridActorControl.Outbound outbound;

  private volatile boolean hasQuorum;
  private final long clusterHealthCheckInterval;

  public Grid(final World world, final AddressFactory addressFactory, final io.vlingo.xoom.cluster.model.Properties clusterProperties, final String gridNodeName) throws Exception {
    super(world, addressFactory, gridNodeName, GridStageBuckets, GridStageInitialCapacity);
    this.hashRing = new MurmurSortedMapHashRing<>(100);
    extenderStartDirectoryScanner();
    this.gridNodeBootstrap = GridNodeBootstrap.boot(this, gridNodeName, clusterProperties, false);
    this.hasQuorum = false;

    this.clusterHealthCheckInterval = clusterProperties.clusterHealthCheckInterval();

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

  @Override
  public void quorumAchieved() {
    this.hasQuorum = true;
  }

  @Override
  public void quorumLost() {
    this.hasQuorum = false;
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
    return __InternalOnlyAccessor.actorOf(this, address);
  }

  /**
   * Relocate local actors. This operation is typically called when node is stopped.
   */
  @Override
  public void relocateActors() {
    final HashRing<Id> copy = this.hashRing.copy();
    this.hashRing.excludeNode(nodeId);

    __InternalOnlyAccessor.allActorAddresses(this).stream()
            .filter(address -> address.isDistributable() && isAssignedTo(copy, address, nodeId))
            .forEach(address -> {
              final Actor actor = __InternalOnlyAccessor.actorOf(this, address);
              final Id toNode = hashRing.nodeOf(address.idString());
              if (toNode != null) { // last node in the cluster?
                relocateActorTo(actor, address, toNode);
              }
            });
  }

  @Override
  public Stage asStage() {
    return this;
  }

  @Override
  protected <T> T actorThunkFor(Class<T> protocol, Definition definition, Address address) {
    final Mailbox actorMailbox = this.allocateMailbox(definition, address, null);
    actorMailbox.suspendExceptFor(GridActorOperations.Resume, Relocatable.class);
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

    __InternalOnlyAccessor.allActorAddresses(this).stream()
        .filter(address ->
            address.isDistributable() && shouldRelocateTo(copy, address, newNode))
        .forEach(address -> {
          final Actor actor = __InternalOnlyAccessor.actorOf(this, address);
          relocateActorTo(actor, address, newNode);
        });
  }

  private void relocateActorTo(Actor actor, Address address, Id toNode) {
    if (!GridActorOperations.isSuspendedForRelocation(actor)) {
      logger.debug("Relocating actor [{}] to [{}]", address, toNode);
      //actor.suspendForRelocation();
      GridActorOperations.suspendForRelocation(actor);
      outbound.relocate(
              toNode,
              nodeId,
              Definition.SerializationProxy.from(actor.definition()),
              address,
              GridActorOperations.supplyRelocationSnapshot(actor) /*actor.provideRelocationSnapshot()*/,
              GridActorOperations.pending(actor));
    }
  }

  private boolean shouldRelocateTo(HashRing<Id> previous, Address address, Id newNode) {
    return isAssignedTo(previous, address, nodeId)
        && isAssignedTo(this.hashRing, address, newNode);
  }

  private static boolean isAssignedTo(HashRing<Id> ring, Address a, Id node) {
    return node.equals(ring.nodeOf(a.idString()));
  }

  @Override
  public QuorumObserver quorumObserver() {
    return this;
  }

  @Override
  public void setOutbound(final GridActorControl.Outbound outbound) {
    this.outbound = outbound;
  }

  @Override
  public void setNodeId(final Id nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public ClassLoader worldClassLoader() {
    return __InternalOnlyAccessor.classLoader(this);
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
    });
    return super.actorProtocolFor(protocol, definition, parent, address, mailbox, maybeSupervisor, logger);
  }

  @Override
  protected ActorProtocolActor<Object>[] actorProtocolFor(Class<?>[] protocols, Definition definition, Actor parent, Address maybeAddress, Mailbox maybeMailbox, Supervisor maybeSupervisor, Logger logger) {
    final Address address = maybeAddress == null ? addressFactory().unique() : maybeAddress;
    final Id node = hashRing.nodeOf(address.idString());
    final Mailbox mailbox = maybeRemoteMailbox(address, definition, maybeMailbox, () -> {
      outbound.start(node, nodeId, protocols[0], address, Definition.SerializationProxy.from(definition)); // TODO remote start all protocols
    });
    return super.actorProtocolFor(protocols, definition, parent, address, mailbox, maybeSupervisor, logger);
  }

  private Mailbox maybeRemoteMailbox(final Address address, final Definition definition, final Mailbox maybeMailbox, final Runnable out) {
    while (!hasQuorum && address.isDistributable()) {
      logger.debug("Mailbox allocation waiting for cluster quorum...");
      try {
        Thread.sleep(clusterHealthCheckInterval);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    final Id node = hashRing.nodeOf(address.idString());
    final Mailbox __mailbox;
    if (node != null && !node.equals(nodeId)) {
      out.run();
      __mailbox = allocateMailbox(definition, address, maybeMailbox);
      if (!__mailbox.isSuspendedFor(GridActorOperations.Resume)) {
        __mailbox.suspendExceptFor(GridActorOperations.Resume, Relocatable.class);
      }
    }
    else {
      __mailbox = maybeMailbox;
    }
    return __mailbox;
  }
}
