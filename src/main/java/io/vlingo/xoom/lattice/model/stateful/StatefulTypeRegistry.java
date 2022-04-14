// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.stateful;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.symbio.StateAdapterProvider;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.StateTypeStateStoreMap;

/**
 * Registry for {@code StatefulEntity} types that holds the {@code StateStore} type
 * and {@code StateAdapter<S,RS>}.
 */
public final class StatefulTypeRegistry {
  static final String INTERNAL_NAME = UUID.randomUUID().toString();

  private final Map<Class<?>,Info<?>> stores = new HashMap<>();

  /**
   * Answer a new {@code StatefulTypeRegistry} after registering all {@code types} with the {@code stateStore}
   * using the default {@code storeName} for each of the {@code types}.
   * @param world the World to which the new StatefulTypeRegistry is registered
   * @param stateStore the StateStore
   * @param types the {@code Class<?>}[] native type of states to be stored
   * @return StatefulTypeRegistry
   */
  public static StatefulTypeRegistry registerAll(final World world, final StateStore stateStore, final Class<?>... types) {
    final StatefulTypeRegistry registry = statefulTypeRegistry(world);

    registry.registerAll(stateStore, types);

    return registry;
  }

  /**
   * Answer the {@code StatefulTypeRegistry} held by the {@code world}.
   * If the registry doesn't exist, a one is instantiated and registered.
   * @param world the World where the StatefulTypeRegistry is held
   * @return StatefulTypeRegistry
   */
  public static StatefulTypeRegistry statefulTypeRegistry(final World world) {
    final StatefulTypeRegistry registry = world.resolveDynamic(INTERNAL_NAME, StatefulTypeRegistry.class);

    if (registry != null) {
      return registry;
    }

    return new StatefulTypeRegistry(world);
  }

  /**
   * Construct my default state and register it with the {@code world}.
   * @param world the World to which I am registered
   */
  public StatefulTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);

    StateAdapterProvider.instance(world);
  }

  /**
   * Construct my default state and register it with the {@code world}.
   * @param world the World to which I am registered
   * @param types the {@code Class<?>}[] native type of states to be stored
   */
  public StatefulTypeRegistry(final World world, final StateStore stateStore, final Class<?>... types) {
    this(world);

    registerAll(stateStore, types);
  }

  /**
   * Answer the {@code Info<S>} of the {@code type}.
   * @param type the {@code Class<?>} identifying the desired {@code Info<S>}
   * @param <S> the store type
   * @return {@code Info<S>}
   */
  @SuppressWarnings("unchecked")
  public <S> Info<S> info(Class<?> type) {
    return (Info<S>) stores.get(type);
  }

  /**
   * Answer myself after registering the {@code info}.
   * @param info the {@code Info<?,?>} to register
   * @return StatefulTypeRegistry
   */
  public StatefulTypeRegistry register(final Info<?> info) {
    StateTypeStateStoreMap.stateTypeToStoreName(info.storeType, info.storeName);
    stores.put(info.storeType, info);
    return this;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void registerAll(final StateStore stateStore, final Class<?>[] types) {
    for (final Class<?> type : types) {
      register(new Info(stateStore, type, type.getSimpleName()));
    }
  }

  /**
   * Holder of registration information.
   * @param <S> the native type of the state
   */
  public static class Info<S> {
    public final StateStore store;
    public final String storeName;
    public final Class<S> storeType;

    /**
     * Construct my default state.
     * @param store the StateStore
     * @param storeType the {@code Class<S>} of the State
     * @param storeName the String name of the store
     */
    public Info(final StateStore store, final Class<S> storeType, final String storeName) {
      this.store = store;
      this.storeType = storeType;
      this.storeName = storeName;
    }

    /**
     * Answer whether or not I am a binary type.
     * @return boolean
     */
    public boolean isBinary() {
      return false;
    }

    /**
     * Answer whether or not I am a text type.
     * @return boolean
     */
    public boolean isText() {
      return false;
    }
  }

  /**
   * Holder of binary registration information.
   * @param <S> the native type of the state
   */
  public static class BinaryInfo<S> extends Info<S> {
    public BinaryInfo(final StateStore store, final Class<S> storeType, final String storeName) {
      super(store, storeType, storeName);
    }

    /*
     * @see io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry.Info#isBinary()
     */
    @Override
    public boolean isBinary() {
      return true;
    }
  }

  /**
   * Holder of text registration information.
   * @param <S> the native type of the state
   */
  public static class TextInfo<S> extends Info<S> {
    public TextInfo(final StateStore store, final Class<S> storeType, final String storeName) {
      super(store, storeType, storeName);
    }

    /*
     * @see io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry.Info#isText()
     */
    @Override
    public boolean isText() {
      return true;
    }
  }
}
