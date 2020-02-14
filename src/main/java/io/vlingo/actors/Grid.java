// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.common.Completes;
import io.vlingo.common.identity.IdentityGeneratorType;
import io.vlingo.lattice.grid.GridNodeBootstrap;
import io.vlingo.lattice.grid.application.OutboundGridActorControl;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.lattice.grid.hashring.MurmurSortedMapHashRing;
import io.vlingo.wire.node.Id;

import java.util.Arrays;

public class Grid extends Stage {

  private final HashRing<Id> hashRing;

  private OutboundGridActorControl outbound;
  private Id nodeId;

  public static Grid start(final String worldName, final String gridNodeName) throws Exception {
    mustNotExist();
    final World world = World.startWithDefaults(worldName);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    GridNodeBootstrap.boot(world, grid, gridNodeName, false);
    return grid;
  }

  public static Grid start(final String worldName, final java.util.Properties properties, final String gridNodeName) throws Exception {
    mustNotExist();
    final World world = World.start(worldName, properties);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    GridNodeBootstrap.boot(world, grid, gridNodeName, false);
    return grid;
  }

  public static Grid start(final String worldName, final Configuration configuration, final String gridNodeName) throws Exception {
    mustNotExist();
    final World world = World.start(worldName, configuration);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    GridNodeBootstrap.boot(world, grid, gridNodeName, false);
    return grid;
  }

  public static Grid start(final World world, final AddressFactory addressFactory, final String gridNodeName) throws Exception {
    mustNotExist();
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    GridNodeBootstrap.boot(world, grid, gridNodeName, false);
    return grid;
  }

  private static void mustNotExist() {
    if (GridNodeBootstrap.exists()) {
      throw new IllegalStateException("Grid already exists.");
    }
  }

  public Grid(final World world, final AddressFactory addressFactory, final String gridNodeName) {
    super(world, addressFactory, gridNodeName);
    this.hashRing = new MurmurSortedMapHashRing<>(100);
    extenderStartDirectoryScanner();
  }

  public void setOutbound(final OutboundGridActorControl outbound) {
    this.outbound = outbound;
  }
  public void setNodeId(final Id nodeId) { this.nodeId = nodeId; }

  @Override
  public <T> T actorFor(final Class<T> protocol, final Definition definition) {
    return actorFor(protocol, definition, addressFactory().unique());
  }

  @Override
  public <T> T actorFor(final Class<T> protocol, final Definition definition, final Address address) {
    if (world().isTerminated()) {
      throw new IllegalStateException("vlingo/lattice: Grid has stopped.");
    }
    if (!address.isDistributable()) {
      throw new IllegalArgumentException("Address is not distributable.");
    }

    final T actor = super.actorFor(protocol, definition, address);

    return actor;
  }

  @Override
  public <T> Completes<T> actorOf(final Class<T> protocol, final Address address) {
    if (!address.isDistributable()) {
      throw new IllegalArgumentException("Address is not distributable.");
    }
    return super.actorOf(protocol, address);
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

  public HashRing<Id> hashRing() {
    return hashRing;
  }

  @Override
  protected <T> ActorProtocolActor<T> actorProtocolFor(Class<T> protocol, Definition definition, Actor parent, Address maybeAddress, Mailbox maybeMailbox, Supervisor maybeSupervisor, Logger logger) {
    final Address address = maybeAddress == null ? addressFactory().unique() : maybeAddress;
    final Id node = hashRing.nodeOf(address.idString());
    if (node != null && !node.equals(nodeId)) {
      outbound.start(node, nodeId, protocol, address, definition.type(), definition.parameters().toArray());
    }
    return super.actorProtocolFor(protocol, definition, parent, address, maybeMailbox, maybeSupervisor, logger);
  }

  @Override
  protected ActorProtocolActor<Object>[] actorProtocolFor(Class<?>[] protocols, Definition definition, Actor parent, Address maybeAddress, Mailbox maybeMailbox, Supervisor maybeSupervisor, Logger logger) {
    final Address address = maybeAddress == null ? addressFactory().unique() : maybeAddress;
    final Id node = hashRing.nodeOf(address.idString());
    if (node != null && !node.equals(nodeId)) {
      outbound.start(node, nodeId, protocols[0], address, definition.type(), definition.parameters().toArray()); // TODO remote start all protocols
    }
    return super.actorProtocolFor(protocols, definition, parent, address, maybeMailbox, maybeSupervisor, logger);
  }

  /**
   * Answers the Actor at the specified Address.
   *
   * @param address the Address of the actor
   * @return the Actor
   */
  Actor actorAt(Address address) {
    return directory.actorOf(address);
  }

  public void nodeJoinedCluster(final Id newNode) {
    HashRing<Id> next = this.hashRing
        .copy()
        .includeNode(newNode);
    directory.addresses().filter(a -> {
      final Id currentNodeOf = this.hashRing.nodeOf(a.idString());
      final Id nextNodeOf = next.nodeOf(a.idString());
      return (currentNodeOf == null || currentNodeOf.equals(nodeId)) && !nextNodeOf.equals(nodeId);
    });
    this.hashRing.includeNode(newNode);
  }
}
