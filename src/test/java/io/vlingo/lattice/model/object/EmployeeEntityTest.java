// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import io.vlingo.actors.World;
import io.vlingo.lattice.model.object.ObjectTypeRegistry.Info;
import io.vlingo.symbio.store.object.MapQueryExpression;
import io.vlingo.symbio.store.object.ObjectStore;
import io.vlingo.symbio.store.object.PersistentObjectMapper;
import io.vlingo.symbio.store.object.inmemory.InMemoryObjectStoreActor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmployeeEntityTest {
  private ObjectTypeRegistry registry;
  private ObjectStore objectStore;
  private World world;

  @Test
  public void testThatEmployeeIdentifiesModifiesRecovers() {
    final Employee employee = world.actorFor(Employee.class, EmployeeEntity.class);

    final EmployeeState state1 = employee.hire("12345", 50000).await();
    assertTrue(state1.persistenceId() > 0);
    assertEquals("12345", state1.number);
    assertEquals(50000, state1.salary);

    final EmployeeState state2 = employee.assign("67890").await();
    assertEquals(state1.persistenceId(), state2.persistenceId());
    assertEquals("67890", state2.number);
    assertEquals(50000, state2.salary);

    final EmployeeState state3 = employee.adjust(55000).await();
    assertEquals(state1.persistenceId(), state3.persistenceId());
    assertEquals("67890", state3.number);
    assertEquals(55000, state3.salary);

    final Employee employeeRecovered = world.actorFor(Employee.class, EmployeeEntity.class, state1.persistenceId());
    final EmployeeState state4 = employeeRecovered.current().await();
    assertEquals(state3, state4);

    // TODO: test reading event entries
  }

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() {
    world = World.startWithDefaults("test-object-entity");
    objectStore = world.actorFor(ObjectStore.class, InMemoryObjectStoreActor.class, new MockDispatcher());

    registry = new ObjectTypeRegistry(world);

    // NOTE: The InMemoryObjectStoreActor implementation currently
    // does not use PersistentObjectMapper, and thus the no-op decl.
    final Info<Employee> employeeInfo =
            new Info(
            objectStore,
            EmployeeState.class,
            "HR-Database",
            MapQueryExpression.using(Employee.class, "find", MapQueryExpression.map("id", "id")),
            PersistentObjectMapper.with(Employee.class, new Object(), new Object()));

    registry.register(employeeInfo);

    objectStore.registerMapper(employeeInfo.mapper);
  }
}
