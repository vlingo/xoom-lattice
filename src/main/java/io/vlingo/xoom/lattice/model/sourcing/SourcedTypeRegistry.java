// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.sourcing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.symbio.Entry;
import io.vlingo.xoom.symbio.EntryAdapter;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.Source;
import io.vlingo.xoom.symbio.State;
import io.vlingo.xoom.symbio.StateAdapter;
import io.vlingo.xoom.symbio.StateAdapterProvider;
import io.vlingo.xoom.symbio.store.dispatch.Dispatchable;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;
import io.vlingo.xoom.symbio.store.journal.Journal;

/**
 * Registry for {@code Sourced} types that holds the {@code Journal} type,
 * {@code EntryAdapterProvider}, and {@code StateAdapterProvider}.
 */
public final class SourcedTypeRegistry {
  static final String INTERNAL_NAME = SourcedTypeRegistry.class.getName();

  private final Map<Class<? extends Actor>, Journal<?>> journals = new ConcurrentHashMap<>();
  private final Map<Class<?>,Info<?>> stores = new ConcurrentHashMap<>();

  /**
   * Answer a new {@code SourcedTypeRegistry} with registered {@code sourcedTypes}, creating
   * the {@code Journal} of type {@code journalType}, registering me with the {@code world}.
   * @param world the World to which I am registered
   * @param journalType the concrete {@code Actor} type of the Journal to create
   * @param dispatcher the {@code Dispatcher<Dispatchable<Entry<?>,State<?>>>} of the journalType
   * @param sourcedTypes all {@code Class<Sourced<?>>} types of to register
   * @param <A> the type of Actor used for the Journal implementation
   * @param <S> the {@code Sourced<?>} types to register
   * @return SourcedTypeRegistry
   */
  @SuppressWarnings({ "unchecked" })
  public static <A extends Actor, S extends Sourced<?>> SourcedTypeRegistry registerAll(
          final World world,
          final Class<A> journalType,
          final Dispatcher<Dispatchable<Entry<?>,State<?>>> dispatcher,
          final Class<S> ... sourcedTypes) {

    final SourcedTypeRegistry registry = sourcedTypeRegistry(world);

    final Journal<?> journal = registry.journalOf(journalType, world, dispatcher);

    registry.registerAll(journal, sourcedTypes);

    return registry;
  }

  /**
   * Answer a new {@code SourcedTypeRegistry} with registered {@code sourcedTypes}, creating
   * the {@code Journal} of type {@code journalType}, registering me with the {@code world}.
   *
   * <p>
   * NOTE: register() is an alias for registerAll().
   * </p>
   *
   * @param world the World to which I am registered
   * @param journalType the concrete {@code Actor} type of the Journal to create
   * @param dispatcher the {@code Dispatcher<Dispatchable<Entry<?>,State<?>>>} of the journalType
   * @param sourcedTypes all {@code Class<Sourced<?>>} types of to register
   * @param <A> the type of Actor used for the Journal implementation
   * @param <S> the {@code Sourced<?>} types to register
   * @return SourcedTypeRegistry
   */
  @SuppressWarnings({ "unchecked" })
  public static <A extends Actor, S extends Sourced<?>> SourcedTypeRegistry register(
          final World world,
          final Class<A> journalType,
          final Dispatcher<Dispatchable<Entry<?>,State<?>>> dispatcher,
          final Class<S> ... sourcedTypes) {

    return registerAll(world, journalType, dispatcher, sourcedTypes);
  }

  /**
   * Answer the {@code SourcedTypeRegistry} held by the {@code world}.
   * @param world the World where the SourcedTypeRegistry is held
   * @return SourcedTypeRegistry
   */
  public static SourcedTypeRegistry sourcedTypeRegistry(final World world) {
    final SourcedTypeRegistry registry = world.resolveDynamic(INTERNAL_NAME, SourcedTypeRegistry.class);

    if (registry != null) {
      return registry;
    }

    return new SourcedTypeRegistry(world);
  }

  /**
   * Construct my default state and register me with the {@code world}.
   * @param world the World to which I am registered
   */
  public SourcedTypeRegistry(final World world) {
    world.registerDynamic(INTERNAL_NAME, this);

    EntryAdapterProvider.instance(world);
  }

  /**
   * Construct my default state with {@code sourcedTypes} creating the {@code Journal}
   * of type {@code journalType}, and register me with the {@code world}.
   * @param world the World to which I am registered
   * @param journalType the concrete {@code Actor} type of the Journal to create
   * @param dispatcher the {@code Dispatcher<Dispatchable<Entry<?>,State<?>>>} of the journalType
   * @param sourcedTypes all {@code Class<Sourced<?>>} types of to register
   * @param <A> the type of Actor used for the Journal implementation
   * @param <S> the {@code Sourced<?>} types to register
   */
  @SuppressWarnings("unchecked")
  public <A extends Actor, S extends Sourced<?>> SourcedTypeRegistry(
          final World world,
          final Class<A> journalType,
          final Dispatcher<Dispatchable<Entry<?>,State<?>>> dispatcher,
          final Class<S> ... sourcedTypes) {

    this(world);

    final Journal<?> journal = journalOf(journalType, world, dispatcher);

    registerAll(journal, sourcedTypes);
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
   * Answer the {@code Journal<?>} of the registered {@code journalType}
   * or a new {@code Journal<?>} if non-existing.
   * @param journalType the concrete {@code Actor} type of the Journal to create
   * @param world the World to which I am registered
   * @param dispatcher the {@code Dispatcher<Dispatchable<Entry<?>,State<?>>>} of the journalType
   * @return {@code Journal<?>}
   */
  public <A extends Actor> Journal<?> journalOf(
          final Class<A> journalType,
          final World world,
          final Dispatcher<Dispatchable<Entry<?>,State<?>>> dispatcher) {

    for (final Class<?> actorType : journals.keySet()) {
      if (actorType == journalType) {
        return journals.get(actorType);
      }
    }

    final Journal<?> journal = world.actorFor(Journal.class, journalType, dispatcher);

    journals.put(journalType, journal);

    return journal;
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

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void registerAll(final Journal<?> journal, final Class<?>[] sourcedTypes) {
    for (Class<?> sourcedType : sourcedTypes) {
      this.register(new Info(journal, sourcedType, sourcedType.getSimpleName()));
    }
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
