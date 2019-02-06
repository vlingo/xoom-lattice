// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.lattice.model.object.ObjectEntityTypeRegistry.Info;
import io.vlingo.symbio.store.object.MapQueryExpression;
import io.vlingo.symbio.store.object.ObjectStore;
import io.vlingo.symbio.store.object.PersistentObjectMapper;
import io.vlingo.symbio.store.object.inmemory.InMemoryObjectStoreActor;

public class PersonEntityTest {
  private ObjectEntityTypeRegistry registry;
  private ObjectStore objectStore;
  private World world;

  @Test
  public void testThatPersonIdentifiesModifiesRecovers() {
    final Person person = world.actorFor(Person.class, PersonEntity.class);

    final PersonState state1 = person.identify("Tom Jones", 78).await();
    assertTrue(state1.persistenceId() > 0);
    assertEquals("Tom Jones", state1.name);
    assertEquals(78, state1.age);

    final PersonState state2 = person.change("Tom J Jones").await();
    assertEquals(state1.persistenceId(), state2.persistenceId());
    assertEquals("Tom J Jones", state2.name);
    assertEquals(78, state2.age);

    final PersonState state3 = person.increaseAge().await();
    assertEquals(state1.persistenceId(), state3.persistenceId());
    assertEquals("Tom J Jones", state3.name);
    assertEquals(79, state3.age);

    final Person personRecovered = world.actorFor(Person.class, PersonEntity.class, state1.persistenceId());
    final PersonState state4 = personRecovered.current().await();
    assertEquals(state3, state4);
  }

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() {
    world = World.startWithDefaults("test-object-entity");
    objectStore = world.actorFor(ObjectStore.class, InMemoryObjectStoreActor.class);

    registry = new ObjectEntityTypeRegistry(world);

    // NOTE: The InMemoryObjectStoreActor implementation currently
    // does not use PersistentObjectMapper, and thus the no-op decl.
    final Info<Person> personInfo =
            new Info(
            objectStore,
            PersonState.class,
            "HR-Database",
            MapQueryExpression.using(Person.class, "find", MapQueryExpression.map("id", "id")),
            PersistentObjectMapper.with(Person.class, new Object(), new Object()));

    registry.register(personInfo);

    objectStore.registerMapper(personInfo.mapper);
  }
}
