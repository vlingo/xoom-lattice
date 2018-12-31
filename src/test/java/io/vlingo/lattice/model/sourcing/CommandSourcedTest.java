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
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournalActor;

public class CommandSourcedTest {
  private Entity entity;
  private Journal<String> journal;
  private MockJournalListener listener;
  private SourcedTypeRegistry registry;
  private Result result;
  private World world;

  @Test
  public void testThatCtorEmits() {
    result.until.completes();
    assertTrue(result.tested1);
    assertEquals(1, result.applied.size());
    assertEquals(DoCommand1.class, result.applied.get(0).getClass());
    assertFalse(result.tested2);
  }

  @Test
  public void testThatEventEmits() {
    result.until.completes();
    assertTrue(result.tested1);
    assertFalse(result.tested2);
    assertEquals(1, result.applied.size());
    assertEquals(DoCommand1.class, result.applied.get(0).getClass());
    result.until = TestUntil.happenings(1);
    entity.doTest2();
    result.until.completes();
    assertEquals(2, result.applied.size());
    assertEquals(DoCommand2.class, result.applied.get(1).getClass());
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    world = World.startWithDefaults("test-cs");
    
    listener = new MockJournalListener();

    journal = world.actorFor(Definition.has(InMemoryJournalActor.class, Definition.parameters(listener)), Journal.class);

    registry = new SourcedTypeRegistry(world);
    registry.register(new Info(journal, TestCommandSourcedEntity.class, TestCommandSourcedEntity.class.getSimpleName()));
    registry.info(TestCommandSourcedEntity.class)
      .register(DoCommand1.class, new DoCommand1Adapter(),
              (type, adapter) -> journal.registerAdapter(type, adapter))
      .register(DoCommand2.class, new DoCommand2Adapter(),
              (type, adapter) -> journal.registerAdapter(type, adapter))
      .register(DoCommand3.class, new DoCommand3Adapter(),
              (type, adapter) -> journal.registerAdapter(type, adapter));


    result = new Result();
    entity = world.actorFor(Definition.has(TestCommandSourcedEntity.class, Definition.parameters(result)), Entity.class);
  }
}
