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

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournalActor;

public class EventSourcedTest {
  private Entity entity;
  private Journal<String> journal;
  private MockJournalListener listener;
  private SourcedTypeRegistry registry;
  private Result result;
  private World world;

  @Test
  public void testThatCtorEmits() {
    result.until = TestUntil.happenings(1);
    entity.doTest1();
    result.until.completes();
    assertTrue(result.tested1);
    assertEquals(1, result.applied.size());
    assertEquals(1, listener.entries.size());
    assertEquals(Test1Happened.class, result.applied.get(0).getClass());
    assertEquals(Test1Happened.class.getName(), listener.entries.get(0).type);
    assertFalse(result.tested2);
  }

  @Test
  public void testThatCommandEmits() {
    result.until = TestUntil.happenings(1);
    entity.doTest1();
    result.until.completes();
    assertTrue(result.tested1);
    assertFalse(result.tested2);
    assertEquals(1, result.applied.size());
    assertEquals(1, listener.entries.size());
    assertEquals(Test1Happened.class, result.applied.get(0).getClass());
    assertEquals(Test1Happened.class.getName(), listener.entries.get(0).type);
    result.until = TestUntil.happenings(1);
    entity.doTest2();
    result.until.completes();
    assertEquals(2, result.applied.size());
    assertEquals(2, listener.entries.size());
    assertEquals(Test2Happened.class, result.applied.get(1).getClass());
    assertEquals(Test2Happened.class.getName(), listener.entries.get(1).type);
  }

  @Test
  public void testThatOutcomeCompletes() {
    result.until = TestUntil.happenings(1);
    entity.doTest1();
    result.until.completes();
    assertTrue(result.tested1);
    assertFalse(result.tested3);
    assertEquals(1, result.applied.size());
    assertEquals(1, listener.entries.size());
    assertEquals(Test1Happened.class, result.applied.get(0).getClass());
    assertEquals(Test1Happened.class.getName(), listener.entries.get(0).type);
    result.until = TestUntil.happenings(2);
    entity.doTest3().andThenConsume(greeting -> {
      assertEquals("hello", greeting);
      result.until.happened();
    });
    result.until.completes();
    assertEquals(2, result.applied.size());
    assertEquals(2, listener.entries.size());
    assertEquals(Test3Happened.class, result.applied.get(1).getClass());
    assertEquals(Test3Happened.class.getName(), listener.entries.get(1).type);
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    world = World.startWithDefaults("test-es");
    
    listener = new MockJournalListener();

    journal = world.actorFor(Journal.class, InMemoryJournalActor.class, listener);
    journal.registerEntryAdapter(Test1Happened.class, new Test1HappenedAdapter());
    journal.registerEntryAdapter(Test2Happened.class, new Test2HappenedAdapter());
    journal.registerEntryAdapter(Test3Happened.class, new Test3HappenedAdapter());

    registry = new SourcedTypeRegistry(world);
    registry.register(new Info(journal, TestEventSourcedEntity.class, TestEventSourcedEntity.class.getSimpleName()));

    result = new Result();
    entity = world.actorFor(Entity.class, TestEventSourcedEntity.class, result);
  }
}
