// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import java.util.Arrays;
import java.util.function.BiFunction;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.actors.AddressFactory;
import io.vlingo.actors.Configuration;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.common.Completes;
import io.vlingo.common.identity.IdentityGeneratorType;
import io.vlingo.lattice.grid.cache.Cache;
import io.vlingo.lattice.grid.cache.CacheNodePoint;
import io.vlingo.lattice.grid.hashring.HashRing;
import io.vlingo.lattice.grid.hashring.HashedNodePoint;
import io.vlingo.lattice.grid.hashring.MurmurArrayHashRing;

public class Grid extends Stage {
  private final Cache cache;
  private final BiFunction<Integer, String, HashedNodePoint<String>> factory;
  private final HashRing<String> hashRing;

  public static Grid startWith(final String worldName, final String gridName) {
    final World world = World.startWithDefaults(worldName);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    return new Grid(world, addressFactory, gridName);
  }

  public static Grid startWith(final String worldName, final java.util.Properties properties, final String gridName) {
    final World world = World.start(worldName, properties);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    return new Grid(world, addressFactory, gridName);
  }

  public static Grid startWith(final String worldName, final Configuration configuration, final String gridName) {
    final World world = World.start(worldName, configuration);
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);
    return new Grid(world, addressFactory, gridName);
  }

  public static Grid startWith(final World world, final AddressFactory addressFactory, final String gridName) {
    return new Grid(world, addressFactory, gridName);
  }

  public Grid(final World world, final AddressFactory addressFactory, final String gridName) {
    super(world, addressFactory, gridName);
    extenderStartDirectoryScanner();
    this.cache = Cache.defaultCache();
    this.factory =  (hash, node) -> { return new CacheNodePoint<String>(this.cache, hash, node); };
    this.hashRing = new MurmurArrayHashRing<>(100, factory);
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

  public void terminate() {
    world().terminate();
  }

  HashRing<String> hashRing() {
    return hashRing;
  }
}
