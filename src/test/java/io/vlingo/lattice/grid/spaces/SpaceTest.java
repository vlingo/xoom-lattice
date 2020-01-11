// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.time.Duration;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.common.Completes;
import io.vlingo.lattice.grid.Grid;
import io.vlingo.lattice.grid.GridNodeBootstrap;

public class SpaceTest {
  private static final String DefaultItem = "ThisIsAnItem";

  private Grid grid;

  @Test
  public void shouldCreateSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-create");
    Assert.assertNotNull(accessor1.spaceFor("test"));
  }

  @Test
  public void shouldPutItemInSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-put");
    final Space space = accessor1.spaceFor("test");
    final Key1 key1 = new Key1("123");
    space.put(key1, Item.of(DefaultItem, Lease.Forever));
    final Optional<KeyItem<String>> item = space.get(key1, Period.Forever).await();
    Assert.assertTrue(item.isPresent());
    Assert.assertEquals(item.get().object, DefaultItem);
  }

  @Test
  public void shouldSweepItemAndEvictQueryFromSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-sweep-evict");
    final Space space = accessor1.spaceFor("test", 1, Duration.ofMillis(100));
    final Key1 key1 = new Key1("123");
    space.put(key1, Item.of(DefaultItem, Lease.of(Duration.ZERO)));
    pause(1);
    final Optional<KeyItem<String>> item = space.get(key1, Period.None).await();
    Assert.assertFalse(item.isPresent());
  }

  @Test
  public void shouldFindItemAfterGetSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-find-after");
    final Space space = accessor1.spaceFor("test", 1, Duration.ofMillis(1_000));
    final Key1 key1 = new Key1("123");
    final Completes<Optional<KeyItem<String>>> completes = space.get(key1, Period.of(10000));
    pause(1);
    space.put(key1, Item.of(DefaultItem, Lease.of(Duration.ofMillis(1_000))));
    final Optional<KeyItem<String>> item = completes.await();
    Assert.assertTrue(item.isPresent());
    Assert.assertEquals(item.get().object, DefaultItem);
  }

  @Test
  public void shouldFailGetItemAfterTakeSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-take");
    final Space space = accessor1.spaceFor("take-test", 1, Duration.ofMillis(1_000));
    final Key1 key1 = new Key1("123");
    final Completes<Optional<KeyItem<String>>> completes = space.take(key1, Period.of(1_000));
    space.put(key1, Item.of(DefaultItem, Lease.Forever));
    final Optional<KeyItem<String>> item = completes.await();
    Assert.assertTrue(item.isPresent());
    Assert.assertEquals(item.get().object, DefaultItem);
    final Optional<KeyItem<String>> noItem = space.get(key1, Period.None).await();
    Assert.assertFalse(noItem.isPresent());
  }

  @Before
  public void setUp() throws Exception {
    GridNodeBootstrap.reset();

    grid = Grid.startWith("test-world", "test-grid");
  }

  @After
  public void tearDown() {
    //grid.terminate();
  }

  private void pause(final int seconds) {
    try {
      Thread.sleep(seconds * 1_000);
    } catch (InterruptedException e) {
      // ignore
    }
  }
}
