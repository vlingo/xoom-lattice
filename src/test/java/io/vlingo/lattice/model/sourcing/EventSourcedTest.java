// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.symbio.BaseEntry;
import io.vlingo.symbio.EntryAdapterProvider;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournalActor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EventSourcedTest {
  private Entity entity;
  private Journal<String> journal;
  private MockJournalDispatcher dispatcher;
  private SourcedTypeRegistry registry;
  private Result result;
  private World world;

  @Test
  public void testThatCtorEmits() {
    final AccessSafely resultAccess = result.afterCompleting(2);
    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

    entity.doTest1();

    assertTrue(resultAccess.readFrom("tested1"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    BaseEntry<String> appendeAt0 = dispatcherAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.typeName());
    assertFalse(resultAccess.readFrom("tested2"));
  }

  @Test
  public void testThatCommandEmits() {
    final AccessSafely resultAccess = result.afterCompleting(2);
    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

    entity.doTest1();

    assertTrue(resultAccess.readFrom("tested1"));
    assertFalse(resultAccess.readFrom("tested2"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    BaseEntry<String> appendeAt0 = dispatcherAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.typeName());

    final AccessSafely resultAccess2 = result.afterCompleting(2);
    final AccessSafely dispatcherAccess2 = dispatcher.afterCompleting(1);

    entity.doTest2();

    assertEquals(2, (int) resultAccess2.readFrom("appliedCount"));
    assertEquals(2, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt1 = resultAccess2.readFrom("appliedAt", 1);
    assertNotNull(appliedAt1);
    assertEquals(Test2Happened.class, appliedAt1.getClass());
    BaseEntry<String> appendeAt1 = dispatcherAccess2.readFrom("appendedAt", 1);
    assertNotNull(appendeAt1);
    assertEquals(Test2Happened.class.getName(), appendeAt1.typeName());
  }

  @Test
  public void testThatOutcomeCompletes() {
    final AccessSafely resultAccess = result.afterCompleting(2);
    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

    entity.doTest1();

    assertTrue(resultAccess.readFrom("tested1"));
    assertFalse(resultAccess.readFrom("tested3"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    BaseEntry<String> appendeAt0 = dispatcherAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.typeName());

    final AccessSafely resultAccess2 = result.afterCompleting(2);
    final AccessSafely dispatcherAccess2 = dispatcher.afterCompleting(1);

    entity.doTest3().andThenConsume(greeting -> {
      assertEquals("hello", greeting);
    });

    assertEquals(2, (int) resultAccess2.readFrom("appliedCount"));
    assertEquals(2, (int) dispatcherAccess2.readFrom("entriesCount"));
    Object appliedAt1 = resultAccess2.readFrom("appliedAt", 1);
    assertNotNull(appliedAt1);
    assertEquals(Test3Happened.class, appliedAt1.getClass());
    BaseEntry<String> appendeAt1 = dispatcherAccess.readFrom("appendedAt", 1);
    assertNotNull(appendeAt1);
    assertEquals(Test3Happened.class.getName(), appendeAt1.typeName());
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    world = World.startWithDefaults("test-es");

    dispatcher = new MockJournalDispatcher();

    EntryAdapterProvider entryAdapterProvider = EntryAdapterProvider.instance(world);

    journal = world.actorFor(Journal.class, InMemoryJournalActor.class, dispatcher);
    entryAdapterProvider.registerAdapter(Test1Happened.class, new Test1HappenedAdapter());
    entryAdapterProvider.registerAdapter(Test2Happened.class, new Test2HappenedAdapter());
    entryAdapterProvider.registerAdapter(Test3Happened.class, new Test3HappenedAdapter());

    registry = new SourcedTypeRegistry(world);
    registry.register(new Info(journal, TestEventSourcedEntity.class, TestEventSourcedEntity.class.getSimpleName()));

    result = new Result();
    entity = world.actorFor(Entity.class, TestEventSourcedEntity.class, result);
  }
}
