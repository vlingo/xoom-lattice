// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.Completes;
import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.EntryAdapterProvider;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.StateAdapter;
import io.vlingo.symbio.StateAdapterProvider;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.inmemory.InMemoryStateStoreActor;

public class StatefulEntityTest {
  private final Random idGenerator = new Random();
  private MockTextDispatcher dispatcher;
  private StatefulTypeRegistry registry;
  private StateAdapterProvider stateAdapterProvider;
  private StateStore store;
  private World world;

  @Test
  public void testThatStatefulEntityPreservesRestores() throws Exception {
    final String entityId = "" + idGenerator.nextInt(10_000);
    final Entity1State state = new Entity1State(entityId, "Sally", 23);
    final AccessSafely access = dispatcher.afterCompleting(3);

    final Entity1 entity1 = world.actorFor(Entity1.class, Entity1Actor.class, state);

    assertEquals(state, entity1.current().await());

    entity1.changeName("Sally Jane");

    Entity1State newState = entity1.current().await();

    assertEquals("Sally Jane", newState.name);

    entity1.increaseAge();

    newState = entity1.current().await();

    assertEquals(24, newState.age);

    final Entity1State identityState = new Entity1State(entityId);

    final Entity1 restoredEntity1 = world.actorFor(Entity1.class, Entity1Actor.class, identityState);

    final Entity1State restoredEntity1State = restoredEntity1.current().await();

    assertNotNull(restoredEntity1State);

    assertEquals(1, (int) access.readFrom("dispatchedStateCount"));
    Set<String> ids = access.readFrom("dispatchedIds");
    assertEquals(1, ids.size());

    final TextState flatState = access.readFrom("dispatchedState", ids.iterator().next());

    assertEquals(new Entity1State(entityId, "Sally Jane", 24), stateAdapterProvider.fromRaw(flatState));

    restoredEntity1.current().andThenConsume(current -> {
      assertEquals(new Entity1State(entityId, "Sally Jane", 24), current);
    });
  }

  @Test
  public void testThatMetadataCallbackPreservesRestores() throws Exception {
    final String entityId = "" + idGenerator.nextInt(10_000);
    final Entity1State state = new Entity1State(entityId, "Sally", 23);
    final AccessSafely access = dispatcher.afterCompleting(3);

    final Entity1 entity1 = world.actorFor(Entity1.class, Entity1MetadataCallbackActor.class, state);

    final Entity1State current1 = entity1.current().await();

    assertEquals(state, current1);

    entity1.changeName("Sally Jane");

    Entity1State newState = entity1.current().await();

    assertEquals("Sally Jane", newState.name);

    entity1.increaseAge();

    newState = entity1.current().await();

    assertEquals(24, newState.age);

    final Entity1State identityState = new Entity1State(entityId);

    final Entity1 restoredEntity1 = world.actorFor(Entity1.class, Entity1MetadataCallbackActor.class, identityState);

    final Entity1State restoredEntity1State = restoredEntity1.current().await();

    assertNotNull(restoredEntity1State);

    assertEquals(1, (int) access.readFrom("dispatchedStateCount"));
    Set<String> ids = access.readFrom("dispatchedIds");
    assertEquals(1, ids.size());

    final TextState flatState = access.readFrom("dispatchedState", ids.iterator().next());

    assertEquals(new Entity1State(entityId, "Sally Jane", 24), stateAdapterProvider.fromRaw(flatState));

    restoredEntity1.current().andThenConsume(current -> {
      assertEquals(new Entity1State(entityId, "Sally Jane", 24), current);
    });
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("stateful-entity");
    dispatcher = new MockTextDispatcher();

    stateAdapterProvider = new StateAdapterProvider(world);
    stateAdapterProvider.registerAdapter(Entity1State.class, new Entity1StateAdapter());
    new EntryAdapterProvider(world);
    registry = new StatefulTypeRegistry(world);

    store = world.actorFor(StateStore.class, InMemoryStateStoreActor.class, Arrays.asList(dispatcher));

    registry.register(new Info<>(store, Entity1State.class, Entity1State.class.getSimpleName()));
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  public static class Entity1StateAdapter implements StateAdapter<Entity1State,State<String>> {
    @Override public int typeVersion() { return 1; }

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
      return new TextState(id, Entity1State.class, typeVersion(), serialization, stateVersion);
    }
  }

  public interface Entity1 {
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
    private static final long serialVersionUID = 1L;

    private Entity1State state;

    public Entity1Actor(final Entity1State state) {
      super(state.id);
      this.state = state;
    }

    @Override
    public void start() {
      if (state.hasState()) {
        apply(state);
      } else {
        restore();
      }
    }

    //===================================
    // Entity1
    //===================================

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

  public static class Entity1MetadataCallbackActor extends StatefulEntity<Entity1State> implements Entity1 {
    private static final long serialVersionUID = 1L;

    private Entity1State state;

    public Entity1MetadataCallbackActor(final Entity1State state) {
      super(state.id);
      this.state = state;
    }

    @Override
    public void start() {
      if (state.hasState()) {
        apply(state, "METADATA", "new");
      } else {
        restore();
      }
    }

    //===================================
    // Entity1
    //===================================

    @Override
    public Completes<Entity1State> current() {
      return completes().with(state.copy());
    }

    @Override
    public void changeName(final String name) {
      apply(state.withName(name), "METADATA", "changeName");
    }

    @Override
    public void increaseAge() {
      apply(state.withAge(state.age + 1), "METADATA", "increaseAge");
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
