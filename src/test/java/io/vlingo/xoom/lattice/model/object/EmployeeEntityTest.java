// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.lattice.model.object.ObjectTypeRegistry.Info;
import io.vlingo.xoom.symbio.store.MapQueryExpression;
import io.vlingo.xoom.symbio.store.object.ObjectStore;
import io.vlingo.xoom.symbio.store.object.StateObjectMapper;
import io.vlingo.xoom.symbio.store.object.inmemory.InMemoryObjectStoreActor;

public class EmployeeEntityTest {
  private ObjectTypeRegistry registry;
  private ObjectStore objectStore;
  private World world;

  @Test
  public void testThatEmployeeIdentifiesModifiesRecovers() {
    final String employeeNumber = "12345";

    final Employee employee = world.actorFor(Employee.class, EmployeeEntity.class, () -> new EmployeeEntity(employeeNumber));

    final EmployeeState state1 = employee.hire(50000).await();
    assertTrue(state1.persistenceId() > 0);
    assertEquals(employeeNumber, state1.number);
    assertEquals(50000, state1.salary);

    final EmployeeState state3 = employee.adjust(55000).await();
    assertEquals(state1.persistenceId(), state3.persistenceId());
    assertEquals(employeeNumber, state3.number);
    assertEquals(55000, state3.salary);

    final Employee employeeRecovered = world.actorFor(Employee.class, EmployeeEntity.class, employeeNumber);
    final EmployeeState state4 = employeeRecovered.current().await();
    assertEquals(state3, state4);

    // TODO: test reading event entries
  }

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() {
    world = World.startWithDefaults("test-object-entity");
    objectStore = world.actorFor(ObjectStore.class, InMemoryObjectStoreActor.class, Arrays.asList(new MockDispatcher()));

    registry = new ObjectTypeRegistry(world);

    // NOTE: The InMemoryObjectStoreActor implementation currently
    // does not use PersistentObjectMapper, and thus the no-op decl.
    final Info<Employee> employeeInfo =
            new Info(
            objectStore,
            EmployeeState.class,
            "HR-Database",
            MapQueryExpression.using(Employee.class, "find", MapQueryExpression.map("number", "number")),
            StateObjectMapper.with(Employee.class, new Object(), new Object()));

    registry.register(employeeInfo);
  }
}
