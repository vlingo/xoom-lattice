// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.stateful;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.serialization.JsonSerialization;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.Source;
import io.vlingo.xoom.symbio.State;
import io.vlingo.xoom.symbio.StateAdapter;
import io.vlingo.xoom.symbio.StateAdapterProvider;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.inmemory.InMemoryStateStoreActor;

public class StatefulEntityRaceTest {
    private static final AtomicInteger raceConditions = new AtomicInteger(0);
    private final Random idGenerator = new Random();
    private MockTextDispatcher dispatcher;
    private World world;

    @Test
    public void testThatStatefulEntityPreservesRestores() throws Exception {
        raceConditions.set(0);

        final String entityId = "" + idGenerator.nextInt(10_000);
        final Entity1State state = new Entity1State(entityId, "Sally", 23);
        //final AccessSafely access = dispatcher.afterCompleting(3);

        final Entity1 entity1 = world.actorFor(Entity1.class, Entity1Actor.class, entityId);
        assertEquals(state, entity1.defineWith(state.name, state.age).await());
        assertEquals(state, entity1.current().await());

        entity1.changeName("Sally Jane");
        Entity1State newState = entity1.current().await();
        assertEquals("Sally Jane", newState.name);

        entity1.increaseAge();
        newState = entity1.current().await();
        assertEquals(24, newState.age);

        final Entity1 restoredEntity1 = world.actorFor(Entity1.class, Entity1Actor.class, entityId);
        final Entity1State restoredEntity1State = restoredEntity1.current().await();
        assertNotNull(restoredEntity1State);

        // check whether race conditions have been reproduced
        assertEquals(0, raceConditions.get());
    }

    @Before
    public void setUp() {
        world = World.startWithDefaults("stateful-entity");
        dispatcher = new MockTextDispatcher();

        StateAdapterProvider stateAdapterProvider = new StateAdapterProvider(world);
        stateAdapterProvider.registerAdapter(Entity1State.class, new Entity1StateAdapter());
        new EntryAdapterProvider(world);
        StatefulTypeRegistry registry = new StatefulTypeRegistry(world);

        StateStore store = world.actorFor(StateStore.class, InMemoryStateStoreActor.class, Arrays.asList(dispatcher));

        registry.register(new StatefulTypeRegistry.Info<>(store, Entity1State.class, Entity1State.class.getSimpleName()));
    }

    @After
    public void tearDown() {
        world.terminate();
    }

    public static class Entity1StateAdapter implements StateAdapter<Entity1State, State<String>> {
        @Override
        public int typeVersion() {
            return 1;
        }

        @Override
        public Entity1State fromRawState(final State<String> raw) {
            return JsonSerialization.deserialized(raw.data, Entity1State.class);
        }

        @Override
        public <ST> ST fromRawState(final State<String> raw, final Class<ST> stateType) {
            return JsonSerialization.deserialized(raw.data, stateType);
        }

        @Override
        public State<String> toRawState(final Entity1State state, final int stateVersion) {
            return toRawState(state.id, state, stateVersion, Metadata.nullMetadata());
        }

        @Override
        public State<String> toRawState(final String id, final Entity1State state, final int stateVersion, final Metadata metadata) {
            final String serialization = JsonSerialization.serialized(state);
            return new State.TextState(id, Entity1State.class, typeVersion(), serialization, stateVersion);
        }
    }

    public interface Entity1 {
        Completes<Entity1State> defineWith(String name, int age);

        Completes<Entity1State> current();

        void changeName(final String name);

        void increaseAge();
    }

    public static class Entity1State {
        public final String id;
        public final String name;
        public final int age;

        public Entity1State(final String id, final String name, final int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public Entity1State(final String id) {
            this(id, null, 0);
        }

        public Entity1State copy() {
            return new Entity1State(id, name, age);
        }

        public boolean hasState() {
            return id != null && name != null && age > 0;
        }

        @Override
        public String toString() {
            return "Entity1State[id=" + id + " name=" + name + " age=" + age + "]";
        }

        public Entity1State withName(final String name) {
            return new Entity1State(this.id, name, this.age);
        }

        public Entity1State withAge(final int age) {
            return new Entity1State(this.id, this.name, age);
        }

        @Override
        public boolean equals(final Object other) {
            if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            final Entity1State otherState = (Entity1State) other;
            return this.id.equals(otherState.id) && this.name.equals(otherState.name) && this.age == otherState.age;
        }
    }

    public static class Entity1Actor extends StatefulEntity<Entity1State> implements Entity1 {
        private final AtomicBoolean runsApply = new AtomicBoolean(false);
        private Entity1State state;

        public Entity1Actor(final String id) {
            super(id);
        }

        @Override
        protected <RT> Completes<RT> apply(final Entity1State state, final String metadataValue, final String operation, final Supplier<RT> andThen) {
            runsApply.compareAndSet(false, true);
            return super.apply(state, metadataValue, operation, andThen);
        }

        @Override
        protected <C, RT> Completes<RT> apply(final Entity1State state, final List<Source<C>> sources, final String metadataValue, final String operation, final Supplier<RT> andThen) {
            runsApply.compareAndSet(false, true);
            return super.apply(state, sources, metadataValue, operation, andThen);
        }

        @Override
        protected void afterApply() {
            runsApply.compareAndSet(true, false);
            super.afterApply();
        }

        @Override
        protected <T> Completes<T> completes() {
            if (runsApply.get()) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            try {
                return super.completes();
            } catch (Exception e) {
                raceConditions.incrementAndGet();
                // Assert.assertNotNull("Race condition has been reproduced!", null);
                throw e;
            }
        }

        //===================================
        // Entity1
        //===================================


        @Override
        public Completes<Entity1State> defineWith(String name, int age) {
            if (state == null) {
                return apply(new Entity1State(id, name, age), "new", () -> state);
            } else {
                return completes().with(state.copy());
            }
        }

        @Override
        public Completes<Entity1State> current() {
            return completes().with(state.copy());
        }

        @Override
        public void changeName(final String name) {
            apply(state.withName(name));
        }

        @Override
        public void increaseAge() {
            apply(state.withAge(state.age + 1));
        }

        //===================================
        // StatefulEntity
        //===================================

        @Override
        protected void state(final Entity1State state) {
            this.state = state;
        }

        @Override
        protected Class<Entity1State> stateType() {
            return Entity1State.class;
        }
    }
}
