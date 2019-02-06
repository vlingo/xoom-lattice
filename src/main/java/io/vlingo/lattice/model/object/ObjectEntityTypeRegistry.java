// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
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
import io.vlingo.symbio.store.object.PersistentObjectMapper;
import io.vlingo.symbio.store.object.QueryExpression;

public final class ObjectEntityTypeRegistry {
  static final String INTERNAL_NAME = UUID.randomUUID().toString();

  private final Map<Class<?>,Info<?>> stores = new HashMap<>();

  public ObjectEntityTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);
  }

  @SuppressWarnings("unchecked")
  public <T> Info<T> info(Class<?> type) {
    return (Info<T>) stores.get(type);
  }

  public ObjectEntityTypeRegistry register(final Info<?> info) {
    stores.put(info.storeType, info);
    return this;
  }

  public static class Info<T> {
    public final PersistentObjectMapper mapper;
    public final QueryExpression queryObjectExpression;
    public final ObjectStore store;
    public final String storeName;
    public final Class<T> storeType;

    public Info(final ObjectStore store, final Class<T> storeType, final String storeName, final QueryExpression queryObjectExpression, final PersistentObjectMapper mapper) {
      this.store = store;
      this.storeType = storeType;
      this.storeName = storeName;
      this.queryObjectExpression = queryObjectExpression;
      this.mapper = mapper;
    }
  }
}
