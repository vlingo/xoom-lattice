// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vlingo.actors.World;
import io.vlingo.symbio.store.object.ObjectStore;
import io.vlingo.symbio.store.object.ObjectStoreRepository;
import io.vlingo.symbio.store.object.QueryExpression;
import io.vlingo.symbio.store.object.StateObjectMapper;

/**
 * Registry for {@code Object} types are stored in an {@code ObjectStore}, using
 * {@code PersistentObjectMapper} for round trip mapping and {@code QueryExpression}
 * single instance retrieval.
 */
public final class ObjectTypeRegistry {
  static final String INTERNAL_NAME = UUID.randomUUID().toString();

  private final Map<Class<?>,Info<?>> stores = new HashMap<>();

  /**
   * Construct my default state and register me with the {@code world}.
   * @param world the World to which I am registered
   */
  public ObjectTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);
  }

  /**
   * Answer the {@code Info<T>} of the {@code type}.
   * @param type the {@code Class<?>} identifying the desired {@code Info<T>}
   * @param <T> the type of Object in the ObjectStore
   * @return {@code Info<T>}
   */
  @SuppressWarnings("unchecked")
  public <T> Info<T> info(Class<?> type) {
    return (Info<T>) stores.get(type);
  }

  /**
   * Answer myself after registering the {@code info}.
   * @param info the {@code Info<?>} to register
   * @return ObjectTypeRegistry
   */
  public ObjectTypeRegistry register(final Info<?> info) {
    stores.put(info.storeType, info);
    return this;
  }

  /**
   * Answer the {@code repository} registered with the {@code Info<T>}
   * as referenced by {@code type}, or {@code null} if there is none.
   * @param type the {@code Class<?>} referencing the {@code Info<T>}
   * @return ObjectStoreRepository
   */
  public ObjectStoreRepository repository(Class<?> type) {
    final Info<?> info = stores.get(type);
    return info.repository;
  }

  /**
   * Holder of registration information.
   * @param <T> the type of persistent Object state of the registration
   */
  public static class Info<T> {
    public final StateObjectMapper mapper;
    public final QueryExpression queryObjectExpression;
    public final ObjectStoreRepository repository;
    public final ObjectStore store;
    public final String storeName;
    public final Class<T> storeType;

    /**
     * Construct my default state.
     * @param store the ObjectStore instance
     * @param storeType the {@code Class<T>} Object type that uses the ObjectStore
     * @param storeName the String name of the ObjectStore
     * @param queryObjectExpression the QueryExpression used to retrieve a single instance
     * @param mapper the PersistentObjectMapper between Object type and persistent type
     */
    public Info(final ObjectStore store, final Class<T> storeType, final String storeName, final QueryExpression queryObjectExpression, final StateObjectMapper mapper) {
      this.store = store;
      this.storeType = storeType;
      this.storeName = storeName;
      this.queryObjectExpression = queryObjectExpression;
      this.mapper = mapper;
      this.repository = null;
    }

    /**
     * Construct my default state.
     * @param repository the ObjectStoreRepository instance
     * @param storeType the {@code Class<T>} Object type that uses the ObjectStore
     * @param storeName the String name of the ObjectStore
     * @param queryObjectExpression the QueryExpression used to retrieve a single instance
     * @param mapper the PersistentObjectMapper between Object type and persistent type
     */
    public Info(final ObjectStoreRepository repository, final Class<T> storeType, final String storeName, final QueryExpression queryObjectExpression, final StateObjectMapper mapper) {
      this.repository = repository;
      this.store = repository.objectStore();
      this.storeType = storeType;
      this.storeName = storeName;
      this.queryObjectExpression = queryObjectExpression;
      this.mapper = mapper;
    }

    /**
     * Answer whether or not I have a {@code repository}.
     * @return boolean
     */
    public boolean hasRepository() {
      return repository != null;
    }
  }
}
