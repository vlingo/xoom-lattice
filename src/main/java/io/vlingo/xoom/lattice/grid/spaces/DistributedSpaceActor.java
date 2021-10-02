// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.lattice.grid.application.GridActorControl;
import io.vlingo.xoom.lattice.util.SerializableFunction;
import io.vlingo.xoom.wire.node.Id;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public class DistributedSpaceActor extends Actor implements DistributedSpace {
  private final String accessorName;
  private final String spaceName;
  private final int totalPartitions;
  private final Duration scanInterval;
  private final Space localSpace;
  private final Grid grid;

  public DistributedSpaceActor(String accessorName, String spaceName, int totalPartitions, Duration scanInterval, Space localSpace, Grid grid) {
    this.accessorName = accessorName;
    this.spaceName = spaceName;
    this.totalPartitions = totalPartitions;
    this.scanInterval = scanInterval;
    this.localSpace = localSpace;
    this.grid = grid;
  }

  @Override
  public <T> Completes<KeyItem<T>> localPut(Key key, Item<T> item) {
    // this method is invoked remotely as well
    logger().debug("Local PUT for " + key + " and " + item.object);
    return localSpace.put(key, item)
            .andThen(keyItem -> {
              completesEventually().with(keyItem);
              return keyItem;
            });
  }

  @Override
  public <T> Completes<KeyItem<T>> localTake(Key key, Period until) {
    // this method is invoked remotely as well
    logger().debug("Local TAKE for " + key);
    Completes<Optional<KeyItem<T>>> localSpaceCompletes = localSpace.take(key, until);

    return localSpaceCompletes.andThen(keyItem -> {
      // Optional is not Serializable; return possibly null instead
      KeyItem<T> maybeNull = keyItem.orElse(null);
      completesEventually().with(maybeNull);
      return maybeNull;
    });
  }

  @Override
  public <T> Completes<T> itemFor(Class<T> protocol, Class<? extends Actor> type, Object... parameters) {
    // see also SpaceItemFactoryRelay#itemFor
    final T actor = grid.actorFor(protocol, Definition.has(type, Arrays.asList(parameters)), grid.addressFactory().unique());
    return completes().with(actor);
  }

  @Override
  public <T> Completes<KeyItem<T>> put(Key key, Item<T> item) {
    final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.localPut(key, item);
    final GridActorControl outbound = grid.getOutbound();
    final String representation = "localPut(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Item<T>)"; // see DistributedSpace__Proxy
    final SerializableFunction<Grid, Actor> actorProvider = newActorProvider();

    for (Id nodeId : grid.allOtherNodes()) {
      Completes<KeyItem<T>> distributedCompletes = Completes.using(scheduler());
      distributedCompletes.andFinallyConsume(keyItem -> logger().debug("Confirmation of distributed space PUT for " + key + " with " + item.object + " from " + nodeId));

      outbound.deliver(nodeId,
              grid.nodeId(),
              Returns.value(distributedCompletes),
              DistributedSpace.class,
              actorProvider,
              consumer,
              representation);
    }

    return localPut(key, item);
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> get(Key key, Period until) {
    Completes<Optional<KeyItem<T>>> localSpaceCompletes = localSpace.get(key, until);

    return localSpaceCompletes.andThen(keyItem -> {
      completesEventually().with(keyItem);
      return keyItem;
    });
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> take(Key key, Period until) {
    final SerializableConsumer<DistributedSpace> consumer = (actor) -> actor.localTake(key, until);
    final GridActorControl outbound = grid.getOutbound();
    final String representation = "localTake(io.vlingo.xoom.lattice.grid.spaces.Key, io.vlingo.xoom.lattice.grid.spaces.Period)"; // see DistributedSpace__Proxy
    final SerializableFunction<Grid, Actor> actorProvider = newActorProvider();

    for (Id nodeId : grid.allOtherNodes()) {
      Completes<KeyItem<T>> distributedCompletes = Completes.using(scheduler());
      distributedCompletes.andFinallyConsume(maybeNull -> logger().debug("Confirmation of distributed space TAKE from " + nodeId));

      outbound.deliver(nodeId,
              grid.nodeId(),
              Returns.value(distributedCompletes),
              DistributedSpace.class,
              actorProvider,
              consumer,
              representation);
    }

    logger().debug("Local TAKE for " + key);
    Completes<Optional<KeyItem<T>>> localSpaceCompletes = localSpace.take(key, until);

    return localSpaceCompletes.andThen(keyItem -> {
      completesEventually().with(keyItem);
      return keyItem;
    });
  }

  private SerializableFunction<Grid, Actor> newActorProvider() {
    final String _accessorName = this.accessorName;
    final String _spaceName = this.spaceName;
    final int _totalPartitions = this.totalPartitions;
    final Duration _scanInterval = this.scanInterval;

    return (_grid) -> {
      Accessor maybeAccessor = Accessor.named(_grid, _accessorName);
      Accessor accessor = maybeAccessor.isDefined()
              ? maybeAccessor
              : Accessor.using(_grid, _accessorName);

      return ((DistributedSpace__Proxy) accessor.distributedSpaceFor(_spaceName, _totalPartitions, _scanInterval)).__actor();
    };
  }
}
