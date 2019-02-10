// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vlingo.actors.World;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.BinaryState;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.StateAdapter;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.StateTypeStateStoreMap;

/**
 * Registry for {@code StatefulEntity} types that holds the {@code StateStore} type
 * and {@code StateAdapter<S,RS>}.
 */
public final class StatefulTypeRegistry {
  static final String INTERNAL_NAME = UUID.randomUUID().toString();

  private final Map<Class<?>,Info<?,?>> stores = new HashMap<>();

  /**
   * Construct my default state and register it with the {@code world}.
   * @param world the World to which I am registered
   */
  public StatefulTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);
  }

  /**
   * Answer the {@code Info<S,RS>} of the {@code type}.
   * @param type the {@code Class<?>} identifying the desired {@code Info<S,RS>}
   * @param <S> the store type
   * @param <RS> the {@code State<RS>} type
   * @return {@code Info<S,RS>}
   */
  @SuppressWarnings("unchecked")
  public <S,RS extends State<?>> Info<S,RS> info(Class<?> type) {
    return (Info<S,RS>) stores.get(type);
  }

  /**
   * Answer myself after registering the {@code info}.
   * @param info the {@code Info<?,?>} to register
   * @return StatefulTypeRegistry
   */
  public StatefulTypeRegistry register(final Info<?,?> info) {
    StateTypeStateStoreMap.stateTypeToStoreName(info.storeType, info.storeName);
    stores.put(info.storeType, info);
    return this;
  }

  /**
   * Holder of registration information.
   * @param <S> the native type of the state
   * @param <RS> the raw {@code State<?>} of the state
   */
  public static class Info<S,RS extends State<?>> {
    public final StateAdapter<S,RS> adapter;
    public final StateStore store;
    public final String storeName;
    public final Class<S> storeType;

    /**
     * Construct my default state.
     * @param store the StateStore
     * @param storeType the {@code Class<S>} of the State
     * @param storeName the String name of the store
     * @param adapter the {@code StateAdapter<S,RS>} adapter
     */
    public Info(final StateStore store, final Class<S> storeType, final String storeName, final StateAdapter<S,RS> adapter) {
      this.store = store;
      this.storeType = storeType;
      this.storeName = storeName;
      this.adapter = adapter;
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
  public static class BinaryInfo<S> extends Info<S,BinaryState> {
    public BinaryInfo(final StateStore store, final Class<S> storeType, final String storeName, final StateAdapter<S,BinaryState> adapter) {
      super(store, storeType, storeName, adapter);
    }

    /*
     * @see io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info#isBinary()
     */
    public boolean isBinary() {
      return true;
    }
  }

  /**
   * Holder of text registration information.
   * @param <S> the native type of the state
   */
  public static class TextInfo<S> extends Info<S,TextState> {
    public TextInfo(final StateStore store, final Class<S> storeType, final String storeName, final StateAdapter<S,TextState> adapter) {
      super(store, storeType, storeName, adapter);
    }

    /*
     * @see io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info#isText()
     */
    public boolean isText() {
      return true;
    }
  }
}
