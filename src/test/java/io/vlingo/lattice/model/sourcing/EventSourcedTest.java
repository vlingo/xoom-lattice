// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;

public class EventSourcedTest {
  private Entity entity;
  private Result result;
  private World world;

  @Test
  public void testThatCtorEmits() {
    result.until.completes();
    assertTrue(result.tested1);
    assertEquals(1, result.applied.size());
    assertEquals(Test1Happened.class, result.applied.get(0).getClass());
    assertFalse(result.tested2);
  }

  @Test
  public void testThatCommandEmits() {
    result.until.completes();
    assertTrue(result.tested1);
    assertFalse(result.tested2);
    assertEquals(1, result.applied.size());
    assertEquals(Test1Happened.class, result.applied.get(0).getClass());
    result.until = TestUntil.happenings(1);
    entity.doTest2();
    result.until.completes();
    assertEquals(2, result.applied.size());
    assertEquals(Test2Happened.class, result.applied.get(1).getClass());
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-es");
    result = new Result();
    entity = world.actorFor(Definition.has(TestEventSourcedEntity.class, Definition.parameters(result)), Entity.class);
  }
}
