// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import io.vlingo.actors.World;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.EntryAdapter;
import io.vlingo.symbio.EntryAdapterProvider;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.State;
import io.vlingo.symbio.StateAdapter;
import io.vlingo.symbio.StateAdapterProvider;
import io.vlingo.symbio.store.journal.Journal;

public final class SourcedTypeRegistry {
  static final String INTERNAL_NAME = UUID.randomUUID().toString();

  private final Map<Class<?>,Info<?>> stores = new ConcurrentHashMap<>();

  public SourcedTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);
  }

  public Info<?> info(final Class<?> type) {
    return stores.get(type);
  }

  public <T> SourcedTypeRegistry register(final Info<T> info) {
    stores.put(info.sourcedType, info);
    return this;
  }

  public static class Info<T> {
    public final EntryAdapterProvider entryAdapterProvider;
    public final StateAdapterProvider stateAdapterProvider;
    public final Journal<T> journal;
    public final String sourcedName;
    public final Class<Sourced<T>> sourcedType;

    public Info(final Journal<T> journal, final Class<Sourced<T>> sourcedType, final String sourcedName) {
      this.journal = journal;
      this.sourcedType = sourcedType;
      this.sourcedName = sourcedName;
      this.entryAdapterProvider = new EntryAdapterProvider();
      this.stateAdapterProvider = new StateAdapterProvider();
    }

    public EntryAdapterProvider entryAdapterProvider() {
      return entryAdapterProvider;
    }

    public StateAdapterProvider stateAdapterProvider() {
      return stateAdapterProvider;
    }

    public Journal<T> journal() {
      return journal;
    }

    public boolean isBinary() {
      return false;
    }

    public boolean isObject() {
      return false;
    }

    public boolean isText() {
      return false;
    }

    public <S extends Source<?>,E extends Entry<?>> Info<T> registerEntryAdapter(final Class<S> sourceType, final EntryAdapter<S,E> adapter) {
      entryAdapterProvider.registerAdapter(sourceType, adapter);
      return this;
    }

    public <S extends Source<?>,E extends Entry<?>> Info<T> registerEntryAdapter(final Class<S> sourceType, final EntryAdapter<S,E> adapter, final BiConsumer<Class<S>,EntryAdapter<S,E>> consumer) {
      entryAdapterProvider.registerAdapter(sourceType, adapter, consumer);
      return this;
    }

    public <S,ST extends State<?>> Info<T> registerStateAdapter(final Class<S> stateType, final StateAdapter<S,ST> adapter) {
      stateAdapterProvider.registerAdapter(stateType, adapter);
      return this;
    }

    public <S,ST extends State<?>> Info<T> registerStateAdapter(final Class<S> stateType, final StateAdapter<S,ST> adapter, final BiConsumer<Class<S>,StateAdapter<S,ST>> consumer) {
      stateAdapterProvider.registerAdapter(stateType, adapter, consumer);
      return this;
    }
  }
}
