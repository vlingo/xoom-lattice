// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.cluster.ClusterProperties;

public class GridSupportsEvictionsTest {

  @Test
  public void testSupportsEvictions() throws Exception {
    final World world = World.startWithDefaults("test-world");

    final io.vlingo.xoom.cluster.model.Properties properties = ClusterProperties.oneNode();

    final Grid grid = Grid.start(world, properties, "node1");
    
    assertFalse(world.stage().supportsEvictions());
    assertTrue(grid.supportsEvictions());
  }
}
