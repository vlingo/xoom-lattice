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

/**
 * Registry for {@code Sourced} types that holds the {@code Journal} type,
 * {@code EntryAdapterProvider}, and {@code StateAdapterProvider}.
 */
public final class SourcedTypeRegistry {
  static final String INTERNAL_NAME = UUID.randomUUID().toString();

  private final Map<Class<?>,Info<?>> stores = new ConcurrentHashMap<>();

  /**
   * Construct my default state and register me with the {@code world}.
   * @param world the World to which I am registered
   */
  public SourcedTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);
  }

  /**
   * Answer the {@code Info<?>} of the {@code type}.
   * @param type the {@code Class<?>} identifying the desired {@code Info<?>}
   * @return {@code Info<?>}
   */
  public Info<?> info(final Class<?> type) {
    return stores.get(type);
  }

  /**
   * Answer myself after registering the {@code info}.
   * @param info the {@code Info<T>} to register
   * @param <T> the typed of Info being registered
   * @return SourcedTypeRegistry
   */
  public <T> SourcedTypeRegistry register(final Info<T> info) {
    stores.put(info.sourcedType, info);
    return this;
  }

  /**
   * Holder of registration information.
   * @param <T> the type of {@code Journal<T>} of the registration
   */
  public static class Info<T> {
    public final EntryAdapterProvider entryAdapterProvider;
    public final StateAdapterProvider stateAdapterProvider;
    public final Journal<T> journal;
    public final String sourcedName;
    public final Class<Sourced<T>> sourcedType;

    /**
     * Construct my default state.
     * @param journal the {@code Journal<T>} of the registration
     * @param sourcedType the {@code Class<Sourced<T>>} of the registration
     * @param sourcedName the String name of the sourcedType
     */
    public Info(final Journal<T> journal, final Class<Sourced<T>> sourcedType, final String sourcedName) {
      this.journal = journal;
      this.sourcedType = sourcedType;
      this.sourcedName = sourcedName;
      this.entryAdapterProvider = new EntryAdapterProvider();
      this.stateAdapterProvider = new StateAdapterProvider();
    }

    /**
     * Answer my {@code EntryAdapterProvider} instance.
     * @return EntryAdapterProvider
     */
    public EntryAdapterProvider entryAdapterProvider() {
      return entryAdapterProvider;
    }

    /**
     * Answer my {@code StateAdapterProvider} instance.
     * @return StateAdapterProvider
     */
    public StateAdapterProvider stateAdapterProvider() {
      return stateAdapterProvider;
    }

    /**
     * Answer my {@code Journal<T>}.
     * @return {@code Journal<T>}
     */
    public Journal<T> journal() {
      return journal;
    }

    /**
     * Answer whether or not I am a binary type.
     * @return boolean
     */
    public boolean isBinary() {
      return false;
    }

    /**
     * Answer whether or not I am an object type.
     * @return boolean
     */
    public boolean isObject() {
      return false;
    }

    /**
     * Answer whether or not I am a text type.
     * @return boolean
     */
    public boolean isText() {
      return false;
    }

    /**
     * Answer myself after registering the {@code adapter}.
     * @param sourceType the {@code Class<S>} of the EntryAdapter to register
     * @param adapter the {@code EntryAdapter<S,E>} to registered
     * @param <S> the {@code Source<?>} extender being registered
     * @param <E> the {@code Entry<?>} extender being registered
     * @return {@code Info<T>}
     */
    public <S extends Source<?>,E extends Entry<?>> Info<T> registerEntryAdapter(final Class<S> sourceType, final EntryAdapter<S,E> adapter) {
      entryAdapterProvider.registerAdapter(sourceType, adapter);
      return this;
    }

    /**
     * Answer myself after registering the {@code adapter} and {@code consumer}.
     * @param sourceType the {@code Class<S>} of the EntryAdapter to register
     * @param adapter the {@code EntryAdapter<S,E>} to registered
     * @param consumer the {@code BiConsumer<Class<S>,EntryAdapter<S,E>>} being registered
     * @param <S> the {@code Source<?>} extender being registered
     * @param <E> the {@code Entry<?>} extender being registered
     * @return {@code Info<T>}
     */
    public <S extends Source<?>,E extends Entry<?>> Info<T> registerEntryAdapter(final Class<S> sourceType, final EntryAdapter<S,E> adapter, final BiConsumer<Class<S>,EntryAdapter<S,E>> consumer) {
      entryAdapterProvider.registerAdapter(sourceType, adapter, consumer);
      return this;
    }

    /**
     * Answer myself after registering the {@code adapter}.
     * @param stateType the {@code Class<S>} of the StateAdapter to register
     * @param adapter the {@code StateAdapter<S,ST>} to registered
     * @param <S> the {@code State<?>} extender being registered
     * @param <ST> the {@code State<?>} extender being registered
     * @return {@code Info<T>}
     */
    public <S,ST extends State<?>> Info<T> registerStateAdapter(final Class<S> stateType, final StateAdapter<S,ST> adapter) {
      stateAdapterProvider.registerAdapter(stateType, adapter);
      return this;
    }

    /**
     * Answer myself after registering the {@code adapter} and {@code consumer}.
     * @param stateType the {@code Class<S>} of the StateAdapter to register
     * @param adapter the {@code StateAdapter<S,ST>} to registered
     * @param consumer the {@code BiConsumer<Class<S>,StateAdapter<S,ST>>} being registered
     * @param <S> the type of {@code State<?>} being registered
     * @param <ST> the {@code State<?>} extender being registered
     * @return {@code Info<T>}
     */
    public <S,ST extends State<?>> Info<T> registerStateAdapter(final Class<S> stateType, final StateAdapter<S,ST> adapter, final BiConsumer<Class<S>,StateAdapter<S,ST>> consumer) {
      stateAdapterProvider.registerAdapter(stateType, adapter, consumer);
      return this;
    }
  }
}
