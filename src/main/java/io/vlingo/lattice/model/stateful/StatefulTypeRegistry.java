// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import java.util.HashMap;
import java.util.Map;

import io.vlingo.symbio.store.state.BinaryStateStore;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.StateTypeStateStoreMap;
import io.vlingo.symbio.store.state.TextStateStore;

public final class StatefulTypeRegistry {
  public static final StatefulTypeRegistry instance = new StatefulTypeRegistry();

  private final Map<Class<?>,Info<?,?>> stores = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <S,R> Info<S,R> info(Class<?> type) {
    return (Info<S,R>) stores.get(type);
  }

  public StatefulTypeRegistry register(final Info<?,?> info) {
    StateTypeStateStoreMap.stateTypeToStoreName(info.storeType, info.storeName);
    stores.put(info.storeType, info);
    return this;
  }

  public static class Info<S,R> {
    public final StateAdapter<S,R> adapter;
    public final StateStore store;
    public final String storeName;
    public final Class<S> storeType;

    public Info(final StateStore store, final Class<S> storeType, final String storeName, final StateAdapter<S,R> adapter) {
      this.store = store;
      this.storeType = storeType;
      this.storeName = storeName;
      this.adapter = adapter;
    }

    public BinaryStateStore binaryStateStore() {
      return (BinaryStateStore) store;
    }

    public boolean isBinary() {
      return false;
    }

    public TextStateStore textStateStore() {
      return (TextStateStore) store;
    }

    public boolean isText() {
      return false;
    }
  }

  public static class BinaryInfo<S> extends Info<S,byte[]> {
    public BinaryInfo(final BinaryStateStore store, final Class<S> storeType, final String storeName, final StateAdapter<S,byte[]> adapter) {
      super(store, storeType, storeName, adapter);
    }

    public boolean isBinary() {
      return true;
    }
  }

  public static class TextInfo<S> extends Info<S,String> {
    public TextInfo(final TextStateStore store, final Class<S> storeType, final String storeName, final StateAdapter<S,String> adapter) {
      super(store, storeType, storeName, adapter);
    }

    public boolean isText() {
      return true;
    }
  }
}
