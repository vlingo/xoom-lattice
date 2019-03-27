// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.Completes;
import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.StateAdapter;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.inmemory.InMemoryStateStoreActor;

public class StatefulEntityTest {
  private final Random idGenerator = new Random();
  private MockTextDispatcher dispatcher;
  private StatefulTypeRegistry registry;
  private StateStore store;
  private World world;

  @Test
  public void testThatStatefulEntityPreservesRestores() throws Exception {
    final String entityId = "" + idGenerator.nextInt(10_000);
    final Entity1State state = new Entity1State(entityId, "Sally", 23);
    final TestUntil until1 = TestUntil.happenings(3);

    final Entity1 entity1 = world.actorFor(Entity1.class, Entity1Actor.class, state, until1);

    entity1.current().andThenConsume(current -> assertEquals(state, current));

    entity1.changeName("Sally Jane");

    entity1.current().andThenConsume(current -> assertEquals("Sally Jane", current.name));

    entity1.increaseAge();

    entity1.current().andThenConsume(current -> { assertEquals(24, current.age); until1.happened(); });

    until1.completes();

    final Entity1State identityState = new Entity1State(entityId);
    final TestUntil until2 = TestUntil.happenings(1);

    final Entity1 restoredEntity1 = world.actorFor(Entity1.class, Entity1Actor.class, identityState, until2);

    until2.completes();

    final TestUntil until3 = TestUntil.happenings(1);

    restoredEntity1.current().andThenConsume(current -> {
      assertEquals(new Entity1State(entityId, "Sally Jane", 24), current);
      until3.happened();
    });

    until3.completes();
  }

  @Test
  public void testThatMetadataCallbackPreservesRestores() throws Exception {
    final String entityId = "" + idGenerator.nextInt(10_000);
    final Entity1State state = new Entity1State(entityId, "Sally", 23);
    final TestUntil until1 = TestUntil.happenings(3);

    final Entity1 entity1 = world.actorFor(Entity1.class, Entity1MetadataCallbackActor.class, state, until1);

    final Entity1State current1 = entity1.current().await();
    
    assertEquals(state, current1);

    entity1.changeName("Sally Jane");

    final String modifiedName = entity1.current().await().name;

    assertEquals("Sally Jane", modifiedName);

    entity1.increaseAge();

    final int age = entity1.current().await().age;

    assertEquals(24, age);

    until1.completes();

    final Entity1State identityState = new Entity1State(entityId);
    final TestUntil until2 = TestUntil.happenings(1);

    final Entity1 restoredEntity1 = world.actorFor(Entity1.class, Entity1MetadataCallbackActor.class, identityState, until2);

    until2.completes();

    final TestUntil until3 = TestUntil.happenings(1);

    restoredEntity1.current().andThenConsume(current -> {
      assertEquals(new Entity1State(entityId, "Sally Jane", 24), current);
      until3.happened();
    });

    until3.completes();
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("stateful-entity");
    dispatcher = new MockTextDispatcher();
    store = world.actorFor(StateStore.class, InMemoryStateStoreActor.class, dispatcher);
    store.registerAdapter(Entity1State.class, new Entity1StateAdapter());
    registry = new StatefulTypeRegistry(world);
    registry.register(
            new Info<Entity1State,State<String>>(
                    store,
                    Entity1State.class,
                    Entity1State.class.getSimpleName(),
                    new Entity1StateAdapter()));
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
    public State<String> toRawState(final Entity1State state, final int stateVersion) {
      return this.toRawState(state, stateVersion, Metadata.nullMetadata());
    }

    @Override
    public State<String> toRawState(final Entity1State state, final int stateVersion, final Metadata metadata) {
      final String serialization = JsonSerialization.serialized(state);
      return new TextState(state.id, Entity1State.class, typeVersion(), serialization, stateVersion);
    }

    @Override
    public <ST> ST fromRawState(State<String> raw, Class<ST> stateType) {
      return JsonSerialization.deserialized(raw.data, stateType);
    }
  }

  public static interface Entity1 {
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
    private Entity1State state;
    private final TestUntil until;

    public Entity1Actor(final Entity1State state, final TestUntil until) {
      this.state = state;
      this.until = until;
    }

    @Override
    public void start() {
      if (state.hasState()) {
        preserve(state);
      } else {
        restore();
      }
      until.happened();
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
      preserve(state.withName(name));
      until.happened();
    }

    @Override
    public void increaseAge() {
      preserve(state.withAge(state.age + 1));
      until.happened();
    }

    //===================================
    // StatefulEntity
    //===================================

    @Override
    protected String id() {
      return state.id;
    }

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
    private Entity1State state;
    private final TestUntil until;

    public Entity1MetadataCallbackActor(final Entity1State state, final TestUntil until) {
      this.state = state;
      this.until = until;
    }

    @Override
    public void start() {
      if (state.hasState()) {
        preserve(state, "METADATA", "new");
      } else {
        restore();
      }
      until.happened();
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
      preserve(state.withName(name), "METADATA", "changeName");
      until.happened();
    }

    @Override
    public void increaseAge() {
      preserve(state.withAge(state.age + 1), "METADATA", "increaseAge");
      until.happened();
    }

    //===================================
    // StatefulEntity
    //===================================

    @Override
    protected String id() {
      return state.id;
    }

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
