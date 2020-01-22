// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.actors.*;
import io.vlingo.cluster.model.CommunicationsHub;
import io.vlingo.common.Completes;
import io.vlingo.common.identity.IdentityGeneratorType;
import io.vlingo.lattice.grid.application.OutboundGridActorControl;
import io.vlingo.lattice.grid.cache.Cache;
import io.vlingo.lattice.grid.cache.CacheNodePoint;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.lattice.grid.hashring.MurmurArrayHashRing;
import io.vlingo.wire.node.Id;

import java.util.Arrays;

public class Grid extends Stage {

  private final Cache cache;
  private final HashRing<Id> hashRing;

  private CommunicationsHub hub;

  public static Grid startWith(final String worldName, final String gridNodeName) throws Exception {
    mustNotExist();
    final World world = World.startWithDefaults(worldName);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    GridNodeBootstrap.boot(world, grid, gridNodeName, false);
    return grid;
  }

  public static Grid startWith(final String worldName, final java.util.Properties properties, final String gridNodeName) throws Exception {
    mustNotExist();
    final World world = World.start(worldName, properties);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    GridNodeBootstrap.boot(world, grid, gridNodeName, false);
    return grid;
  }

  public static Grid startWith(final String worldName, final Configuration configuration, final String gridNodeName) throws Exception {
    mustNotExist();
    final World world = World.start(worldName, configuration);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    final Grid grid = new Grid(world, addressFactory, gridNodeName);
    GridNodeBootstrap.boot(world, grid, gridNodeName, false);
    return grid;
  }

  public static Grid startWith(final World world, final AddressFactory addressFactory, final String gridNodeName) throws Exception {
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
    this.cache = Cache.defaultCache();
    this.hashRing = new MurmurArrayHashRing<>(100,
        (hash, node) ->
            new CacheNodePoint<>(this.cache, hash, node));
    extenderStartDirectoryScanner();
  }

  public void setHub(final CommunicationsHub hub) {
    this.hub = hub;
  }

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
  public <T> T actorFor(final Class<T> protocol, final Class<? extends Actor> type, final Object...parameters) {
    if (world().isTerminated()) {
      throw new IllegalStateException("vlingo/lattice: Grid has stopped.");
    }

    final T actor = super.actorFor(protocol, Definition.has(type, Arrays.asList(parameters)), addressFactory().unique());

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
        new GridMailbox(mailbox, Id.of(1),
            address, hashRing,
            new OutboundGridActorControl(hub), c -> {});
  }

  public void terminate() {
    world().terminate();
  }

  HashRing<Id> hashRing() {
    return hashRing;
  }
}
