// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.UUID;

import io.vlingo.common.identity.IdentityGeneratorType;
import io.vlingo.lattice.grid.GridNodeBootstrap;
import io.vlingo.lattice.grid.application.OutboundGridActorControl;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.lattice.grid.hashring.MurmurSortedMapHashRing;
import io.vlingo.wire.node.Id;

public class Grid extends Stage {
  private static final String INSTANCE_NAME = UUID.randomUUID().toString();

  public static Grid instance(World world) {
    return world.resolveDynamic(INSTANCE_NAME, Grid.class);
  }

  private final HashRing<Id> hashRing;

  private GridNodeBootstrap gridNodeBootstrap;
  private OutboundGridActorControl outbound;
  private Id nodeId;

  public static Grid startWith(final String worldName, final String gridNodeName) throws Exception {
    final World world = World.startWithDefaults(worldName);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    grid.gridNodeBootstrap(GridNodeBootstrap.boot(world, grid, gridNodeName, false));
    return grid;
  }

  public static Grid startWith(final String worldName, final java.util.Properties properties, final String gridNodeName) throws Exception {
    final World world = World.start(worldName, properties);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    grid.gridNodeBootstrap(GridNodeBootstrap.boot(world, grid, gridNodeName, false));
    return grid;
  }

  public static Grid startWith(final String worldName, final Configuration configuration, final String gridNodeName) throws Exception {
    final World world = World.start(worldName, configuration);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    grid.gridNodeBootstrap(GridNodeBootstrap.boot(world, grid, gridNodeName, false));
    return grid;
  }

  public static Grid startWith(final World world, final AddressFactory addressFactory, final String gridNodeName) throws Exception {
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    grid.gridNodeBootstrap(GridNodeBootstrap.boot(world, grid, gridNodeName, false));
    return grid;
  }

  public Grid(final World world, final AddressFactory addressFactory, final String gridNodeName) {
    super(world, addressFactory, gridNodeName);
    this.hashRing = new MurmurSortedMapHashRing<>(100);
    extenderStartDirectoryScanner();
    world.registerDynamic(INSTANCE_NAME, this);
  }

  public void setOutbound(final OutboundGridActorControl outbound) {
    this.outbound = outbound;
  }
  public void setNodeId(final Id nodeId) { this.nodeId = nodeId; }

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
  public <T> T actorThunkFor(Class<T> protocol, Definition definition, Address address) {
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
  protected <T> ActorProtocolActor<T> actorProtocolFor(Class<T> protocol, Definition definition, Actor parent, Address maybeAddress, Mailbox maybeMailbox, Supervisor maybeSupervisor, Logger logger) {
    final Address address = maybeAddress == null ? addressFactory().unique() : maybeAddress;
    final Id node = hashRing.nodeOf(address.idString());
    final Mailbox mailbox = maybeRemoteMailbox(address, definition, maybeMailbox, () -> {
      outbound.start(node, nodeId, protocol, address, Definition.SerializationProxy.from(definition));
    });
    return super.actorProtocolFor(protocol, definition, parent, address, mailbox, maybeSupervisor, logger);
  }

  private Mailbox maybeRemoteMailbox(final Address address, final Definition definition, final Mailbox maybeMailbox, final Runnable out) {
    final Id node = hashRing.nodeOf(address.idString());
    final Mailbox __mailbox;
    if (node != null && !node.equals(nodeId)) {
      out.run();
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

  @Override
  protected ActorProtocolActor<Object>[] actorProtocolFor(Class<?>[] protocols, Definition definition, Actor parent, Address maybeAddress, Mailbox maybeMailbox, Supervisor maybeSupervisor, Logger logger) {
    final Address address = maybeAddress == null ? addressFactory().unique() : maybeAddress;
    final Id node = hashRing.nodeOf(address.idString());
    final Mailbox mailbox = maybeRemoteMailbox(address, definition, maybeMailbox, () -> {
      outbound.start(node, nodeId, protocols[0], address, Definition.SerializationProxy.from(definition)); // TODO remote start all protocols
    });
    return super.actorProtocolFor(protocols, definition, parent, address, mailbox, maybeSupervisor, logger);
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

  public void nodeJoined(final Id newNode) {
    if (nodeId.equals(newNode)) {
      // self is added to the hash-ring on GridNode#start
      return;
    }

    final HashRing<Id> current = this.hashRing.copy();
    this.hashRing.includeNode(newNode);

    directory.addresses().stream()
        .filter(a ->
            a.isDistributable() && isReassigned(current, a))
        .forEach((address -> {
          final GridActor<?> actor = ((GridActor<?>) directory.actorOf(address));
          if (!actor.isSuspended()) {
            actor.suspend();
            outbound.relocate(
                newNode, nodeId, Definition.SerializationProxy.from(actor.definition()),
                address, actor.provideRelocationSnapshot(), actor.pending());
          }
        }));
  }

  private boolean isReassigned(HashRing<Id> current, Address a) {
    return isAssignedToSelf(a, current) && !isAssignedToSelf(a, this.hashRing);
  }

  private boolean isAssignedToSelf(Address a, HashRing<Id> R) {
    return nodeId.equals(R.nodeOf(a.idString()));
  }

  GridNodeBootstrap gridNodeBootstrap() {
    return gridNodeBootstrap;
  }

  void gridNodeBootstrap(final GridNodeBootstrap gridNodeBootstrap) {
    if (gridNodeBootstrap == null) {
      throw new IllegalStateException("Grid already exists.");
    }
    this.gridNodeBootstrap = gridNodeBootstrap;
  }
}
