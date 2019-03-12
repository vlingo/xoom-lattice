// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.symbio.Entry;
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
    final AccessSafely resultAccess = result.afterCompleting(1);
    final AccessSafely listenerAccess = listener.afterCompleting(1);

    entity.doTest1();
    
    assertTrue(resultAccess.readFrom("tested1"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) listenerAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    Entry<String> appendeAt0 = listenerAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.type);
    assertFalse(resultAccess.readFrom("tested1"));
  }

  @Test
  public void testThatCommandEmits() {
    final AccessSafely resultAccess = result.afterCompleting(1);
    final AccessSafely listenerAccess = listener.afterCompleting(1);

    entity.doTest1();
    
    assertTrue(resultAccess.readFrom("tested1"));
    assertFalse(resultAccess.readFrom("tested2"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) listenerAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    Entry<String> appendeAt0 = listenerAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.type);
    
    final AccessSafely resultAccess2 = result.afterCompleting(1);
    final AccessSafely listenerAccess2 = listener.afterCompleting(1);
    
    entity.doTest2();
    
    assertEquals(2, (int) resultAccess2.readFrom("appliedCount"));
    assertEquals(2, (int) listenerAccess.readFrom("entriesCount"));
    Object appliedAt1 = resultAccess2.readFrom("appliedAt", 1);
    assertNotNull(appliedAt1);
    assertEquals(Test2Happened.class, appliedAt1.getClass());
    Entry<String> appendeAt1 = listenerAccess2.readFrom("appendedAt", 1);
    assertNotNull(appendeAt1);
    assertEquals(Test2Happened.class.getName(), appendeAt1.type);
  }

  @Test
  public void testThatOutcomeCompletes() {
    final AccessSafely resultAccess = result.afterCompleting(1);
    final AccessSafely listenerAccess = listener.afterCompleting(1);
    
    entity.doTest1();
    
    assertTrue(resultAccess.readFrom("tested1"));
    assertFalse(resultAccess.readFrom("tested3"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) listenerAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    Entry<String> appendeAt0 = listenerAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.type);
    
    final AccessSafely resultAccess2 = result.afterCompleting(2);
    final AccessSafely listenerAccess2 = listener.afterCompleting(1);
    
    entity.doTest3().andThenConsume(greeting -> {
      assertEquals("hello", greeting);
    });
    
    assertEquals(2, (int) resultAccess2.readFrom("appliedCount"));
    assertEquals(2, (int) listenerAccess2.readFrom("entriesCount"));
    Object appliedAt1 = resultAccess2.readFrom("appliedAt", 1);
    assertNotNull(appliedAt1);
    assertEquals(Test3Happened.class, appliedAt1.getClass());
    Entry<String> appendeAt1 = listenerAccess.readFrom("appendedAt", 1);
    assertNotNull(appendeAt1);
    assertEquals(Test3Happened.class.getName(), appendeAt1.type);
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
