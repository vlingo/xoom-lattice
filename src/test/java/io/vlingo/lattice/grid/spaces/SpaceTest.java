// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.lattice.grid.Grid;

public class SpaceTest {
  private static final String DefaultItem = "ThisIsAnItem";

  private Grid grid;

  @Test
  public void shouldCreateSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-accessor");
    Assert.assertNotNull(accessor1.spaceFor("test"));
  }

  @Test
  public void shouldPutItemInSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-accessor");
    final Space space = accessor1.spaceFor("test");
    final Key1 key1 = new Key1("123");
    space.put(key1, Item.of(DefaultItem, Lease.Forever));
    final Optional<KeyItem<String>> item = space.get(key1, Period.Forever).await();
    Assert.assertTrue(item.isPresent());
    Assert.assertEquals(item.get().object, DefaultItem);
  }

  @Before
  public void setUp() {
    grid = Grid.startWith("test-world", "test-grid");
  }

  @After
  public void tearDown() {
    //grid.terminate();
  }
}
