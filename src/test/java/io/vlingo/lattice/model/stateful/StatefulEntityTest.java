// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.Completes;
import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.StateAdapter;
import io.vlingo.symbio.store.state.TextStateStore;
import io.vlingo.symbio.store.state.inmemory.InMemoryTextStateStoreActor;

public class StatefulEntityTest {
  private MockTextDispatcher dispatcher;
  private StatefulTypeRegistry registry;
  private TextStateStore store;
  private World world;

  @Test
  public void testThatStatefulEntityPreservesRestores() throws Exception {
    final Entity1State state = new Entity1State("123", "Sally", 23);
    final TestUntil until1 = TestUntil.happenings(3);

    final Entity1 entity1 =
            world.actorFor(Definition.has(Entity1Actor.class, Definition.parameters(state, until1)), Entity1.class);

    entity1.current().andThenConsume((Entity1State current) -> assertEquals(state, current));

    entity1.changeName("Sally Jane");

    entity1.current().andThenConsume((Entity1State current) -> assertEquals("Sally Jane", current.name));

    entity1.increaseAge();

    entity1.current().andThenConsume((Entity1State current) -> assertEquals(24, current.age));

    until1.completes();

    final Entity1State identityState = new Entity1State("123");
    final TestUntil until2 = TestUntil.happenings(1);

    final Entity1 restoredEntity1 =
            world.actorFor(Definition.has(Entity1Actor.class, Definition.parameters(identityState, until2)), Entity1.class);

    until2.completes();

    final TestUntil until3 = TestUntil.happenings(1);

    restoredEntity1.current().andThenConsume(current -> {
      assertEquals(new Entity1State("123", "Sally Jane", 24), current);
      until3.happened();
    });

    until3.completes();
  }

  @Test
  public void testThatMetadataCallbackPreservesRestores() throws Exception {
    final Entity1State state = new Entity1State("123", "Sally", 23);
    final TestUntil until1 = TestUntil.happenings(3);

    final Entity1 entity1 =
            world.actorFor(Definition.has(Entity1MetadataCallbackActor.class, Definition.parameters(state, until1)), Entity1.class);

    entity1.current().andThenConsume((Entity1State current) -> assertEquals(state, current));

    entity1.changeName("Sally Jane");

    entity1.current().andThenConsume((Entity1State current) -> assertEquals("Sally Jane", current.name));

    entity1.increaseAge();

    entity1.current().andThenConsume((Entity1State current) -> assertEquals(24, current.age));

    until1.completes();

    final Entity1State identityState = new Entity1State("123");
    final TestUntil until2 = TestUntil.happenings(1);

    final Entity1 restoredEntity1 =
            world.actorFor(Definition.has(Entity1MetadataCallbackActor.class, Definition.parameters(identityState, until2)), Entity1.class);

    until2.completes();

    final TestUntil until3 = TestUntil.happenings(1);

    restoredEntity1.current().andThenConsume(current -> {
      assertEquals(new Entity1State("123", "Sally Jane", 24), current);
      until3.happened();
    });

    until3.completes();
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("stateful-entity");
    dispatcher = new MockTextDispatcher();
    store = world.actorFor(Definition.has(InMemoryTextStateStoreActor.class, Definition.parameters(dispatcher)), TextStateStore.class);
    registry = new StatefulTypeRegistry(world);
    registry.register(
            new Info<Entity1State,String>(
                    store,
                    Entity1State.class,
                    Entity1State.class.getSimpleName(),
                    new Entity1StateAdapter()));
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  public static class Entity1StateAdapter implements StateAdapter<Entity1State,String> {
    @Override
    public Entity1State fromRaw(final String raw, final int stateVersion, final int typeVersion) {
      return JsonSerialization.deserialized(raw, Entity1State.class);
    }

    @Override
    public String toRaw(final Entity1State state, final int stateVersion, final int typeVersion) {
      return JsonSerialization.serialized(state);
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

  public static class Entity1Actor extends StatefulEntity<Entity1State,String> implements Entity1 {
    private Entity1State state;
    private int stateVersion = 0;
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
      return completes().with(state);
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
    public String id() {
      return state.id;
    }

    @Override
    public void state(final Entity1State state, final int stateVersion) {
      this.state = state;
      this.stateVersion = stateVersion;
    }

    @Override
    public Class<Entity1State> stateType() {
      return Entity1State.class;
    }

    @Override
    public int stateVersion() {
      return stateVersion;
    }

    @Override
    public int typeVersion() {
      return 1;
    }
  }

  public static class Entity1MetadataCallbackActor extends StatefulEntity<Entity1State,String> implements Entity1 {
    private Entity1State state;
    private int stateVersion = 0;
    private final TestUntil until;

    public Entity1MetadataCallbackActor(final Entity1State state, final TestUntil until) {
      this.state = state;
      this.until = until;
    }

    @Override
    public void start() {
      if (state.hasState()) {
        preserve(state, "METADATA", "new", (state, version) -> state(state, version));
      } else {
        restore((state, version) -> state(state, version));
      }
      until.happened();
    }

    //===================================
    // Entity1
    //===================================

    @Override
    public Completes<Entity1State> current() {
      return completes().with(state);
    }

    @Override
    public void changeName(final String name) {
      preserve(state.withName(name), "METADATA", "changeName", (state, version) -> {
        state(state, version);
      });
      until.happened();
    }

    @Override
    public void increaseAge() {
      preserve(state.withAge(state.age + 1), "METADATA", "increaseAge", (state, version) -> {
        state(state, version);
      });
      until.happened();
    }

    //===================================
    // StatefulEntity
    //===================================

    @Override
    public String id() {
      return state.id;
    }

    @Override
    public void state(final Entity1State state, final int stateVersion) {
      this.state = state;
      this.stateVersion = stateVersion;
    }

    @Override
    public Class<Entity1State> stateType() {
      return Entity1State.class;
    }

    @Override
    public int stateVersion() {
      return stateVersion;
    }

    @Override
    public int typeVersion() {
      return 1;
    }
  }
}
