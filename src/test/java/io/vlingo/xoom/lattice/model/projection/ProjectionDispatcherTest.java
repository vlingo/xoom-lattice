// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Protocols;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.lattice.model.stateful.StatefulEntityTest.Entity1State;
import io.vlingo.xoom.lattice.model.stateful.StatefulEntityTest.Entity1StateAdapter;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.StateAdapterProvider;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;
import io.vlingo.xoom.symbio.store.dispatch.DispatcherControl;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.StateTypeStateStoreMap;
import io.vlingo.xoom.symbio.store.state.inmemory.InMemoryStateStoreActor;

@SuppressWarnings("rawtypes")
public abstract class ProjectionDispatcherTest {
  protected Dispatcher dispatcher;
  protected DispatcherControl dispatcherControl;
  protected ProjectionDispatcher projectionDispatcher;
  protected StateStore store;
  protected World world;

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-store");

    final StateAdapterProvider stateAdapterProvider = new StateAdapterProvider(world);
    stateAdapterProvider.registerAdapter(Entity1State.class, new Entity1StateAdapter());
    new EntryAdapterProvider(world);

    StateTypeStateStoreMap.stateTypeToStoreName(Entity1.class, Entity1.class.getSimpleName());
    StateTypeStateStoreMap.stateTypeToStoreName(Entity2.class, Entity2.class.getSimpleName());

    final Protocols dispatcherProtocols =
            world.actorFor(
                    new Class<?>[] { dispatcherInterfaceClass(), ProjectionDispatcher.class },
                    projectionDispatcherClass());

    final Protocols.Two<Dispatcher, ProjectionDispatcher> dispatchers = Protocols.two(dispatcherProtocols);
    dispatcher = dispatchers._1;
    projectionDispatcher = dispatchers._2;

    final Protocols storeProtocols =
            world.actorFor(
                    new Class<?>[] { stateStoreInterfaceClass(), DispatcherControl.class },
                    InMemoryStateStoreActor.class,
                    Arrays.asList(dispatcher));

    final Protocols.Two<StateStore, DispatcherControl> storeWithControl = Protocols.two(storeProtocols);
    store = storeWithControl._1;
    dispatcherControl = storeWithControl._2;
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  protected abstract Class<? extends Dispatcher> dispatcherInterfaceClass();
  protected abstract Class<? extends Actor> projectionDispatcherClass();
  protected abstract Class<? extends StateStore> stateStoreInterfaceClass();

  @SuppressWarnings("unchecked")
  protected <T> T store() {
    return (T) store;
  }
}
