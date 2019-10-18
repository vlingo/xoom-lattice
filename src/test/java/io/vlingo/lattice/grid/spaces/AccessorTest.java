// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.time.Duration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.lattice.grid.Grid;

public class AccessorTest {
  private Grid grid;

  @Test
  public void shouldCreateAccessor() {
    final Accessor accessor1 = Accessor.using(grid, "test-accessor");
    Assert.assertNotNull(accessor1);
    Assert.assertTrue(accessor1.isDefined());
    Assert.assertFalse(accessor1.isNotDefined());
    final Accessor accessor2 = Accessor.named(grid, "test-accessor");
    Assert.assertNotNull(accessor2);
    Assert.assertEquals(accessor1.name, accessor2.name);
  }

  @Test
  public void shouldCreateNamedSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-accessor");
    Assert.assertNotNull(accessor1.spaceFor("test"));
  }

  @Test
  public void shouldCreateNamedLongSweepIntervalSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-accessor");
    Assert.assertNotNull(accessor1.spaceFor("test", 1000));
  }

  @Test
  public void shouldCreateNamedDurationSweepIntervalSpace() {
    final Accessor accessor1 = Accessor.using(grid, "test-accessor");
    Assert.assertNotNull(accessor1.spaceFor("test", 1, Duration.ofMillis(1000)));
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
