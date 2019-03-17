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
    final AccessSafely access = result.afterCompleting(1);

    entity.doTest1();
    
    assertTrue(access.readFrom("tested1"));
    assertEquals(1, (int) access.readFrom("appliedCount"));
    Object appliedAt0 = access.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(DoCommand1.class, appliedAt0.getClass());
    assertFalse(access.readFrom("tested2"));
  }

  @Test
  public void testThatEventEmits() {
    final AccessSafely access = result.afterCompleting(1);
    
    entity.doTest1();

    assertTrue(access.readFrom("tested1"));
    assertFalse(access.readFrom("tested2"));
    assertEquals(1, (int) access.readFrom("appliedCount"));
    Object appliedAt0 = access.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(DoCommand1.class, appliedAt0.getClass());
    
    final AccessSafely access2 = result.afterCompleting(1);
    
    entity.doTest2();

    assertEquals(2, (int) access2.readFrom("appliedCount"));
    Object appliedAt1 = access2.readFrom("appliedAt", 1);
    assertNotNull(appliedAt1);
    assertEquals(DoCommand2.class, appliedAt1.getClass());
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    world = World.startWithDefaults("test-cs");
    
    listener = new MockJournalListener();

    journal = world.actorFor(Journal.class, InMemoryJournalActor.class, listener);

    registry = new SourcedTypeRegistry(world);
    registry.register(new Info(journal, TestCommandSourcedEntity.class, TestCommandSourcedEntity.class.getSimpleName()));
    registry.info(TestCommandSourcedEntity.class)
      .registerEntryAdapter(DoCommand1.class, new DoCommand1Adapter(),
              (type, adapter) -> journal.registerEntryAdapter(type, adapter))
      .registerEntryAdapter(DoCommand2.class, new DoCommand2Adapter(),
              (type, adapter) -> journal.registerEntryAdapter(type, adapter))
      .registerEntryAdapter(DoCommand3.class, new DoCommand3Adapter(),
              (type, adapter) -> journal.registerEntryAdapter(type, adapter));


    result = new Result();
    entity = world.actorFor(Entity.class, TestCommandSourcedEntity.class, result);
  }
}
