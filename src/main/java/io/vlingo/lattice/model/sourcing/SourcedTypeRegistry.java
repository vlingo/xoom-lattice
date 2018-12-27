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
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.Journal.ObjectJournal;
import io.vlingo.symbio.store.journal.Journal.TextJournal;
import io.vlingo.symbio.store.state.BinaryStateStore;
import io.vlingo.symbio.store.state.TextStateStore;

public final class SourcedTypeRegistry {
  static final String INTERNAL_NAME = UUID.randomUUID().toString();

  private final Map<Class<?>,Info<?,?>> stores = new ConcurrentHashMap<>();

  public SourcedTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);
  }

  @SuppressWarnings("unchecked")
  public <S,R> Info<S,R> info(Class<S> type) {
    return (Info<S,R>) stores.get(type);
  }

  public <S,R> SourcedTypeRegistry register(final Info<S,R> info) {
    stores.put(info.sourcedType, info);
    return this;
  }

  public static class Info<S,R> {
    public final EntryAdapterProvider entryAdapterProvider;
    public final Journal<R> journal;
    public final String sourcedName;
    public final Class<S> sourcedType;

    public Info(final Journal<R> journal, final Class<S> sourcedType, final String sourcedName) {
      this.journal = journal;
      this.sourcedType = sourcedType;
      this.sourcedName = sourcedName;
      this.entryAdapterProvider = new EntryAdapterProvider();
    }

    public EntryAdapterProvider entryAdapterProvider() {
      return entryAdapterProvider;
    }

    @SuppressWarnings("unchecked")
    public <T> Journal<T> journal() {
      return (Journal<T>) journal;
    }

    public BinaryStateStore binaryJournal() {
      return (BinaryStateStore) journal;
    }

    public boolean isBinary() {
      return false;
    }

    public ObjectJournal objectJournal() {
      return (ObjectJournal) journal;
    }

    public boolean isObject() {
      return false;
    }

    public TextStateStore textJournal() {
      return (TextStateStore) journal;
    }

    public boolean isText() {
      return false;
    }

    public <T extends Source<?>,E extends Entry<?>> Info<S,R> register(final Class<T> sourceType, final EntryAdapter<T,E> adapter) {
      entryAdapterProvider.registerAdapter(sourceType, adapter);
      return this;
    }

    public <T extends Source<?>,E extends Entry<?>> Info<S,R> register(final Class<T> sourceType, final EntryAdapter<T,E> adapter, final BiConsumer<Class<T>,EntryAdapter<T,E>> consumer) {
      entryAdapterProvider.registerAdapter(sourceType, adapter, consumer);
      return this;
    }
  }

  public static class BinaryInfo<S> extends Info<S,byte[]> {
    @SuppressWarnings("unchecked")
    public BinaryInfo(final BinaryStateStore journal, final Class<S> sourcedType, final String sourcedName) {
      super((Journal<byte[]>) journal, sourcedType, sourcedName);
    }

    public boolean isBinary() {
      return true;
    }
  }

  public static class ObjectInfo<S> extends Info<S,Object> {
    public ObjectInfo(final ObjectJournal journal, final Class<S> sourcedType, final String sourcedName) {
      super((Journal<Object>) journal, sourcedType, sourcedName);
    }

    public boolean isObject() {
      return true;
    }
  }

  public static class TextInfo<S> extends Info<S,String> {
    public TextInfo(final TextJournal journal, final Class<S> sourcedType, final String sourcedName) {
      super((Journal<String>) journal, sourcedType, sourcedName);
    }

    public boolean isText() {
      return true;
    }
  }
}
