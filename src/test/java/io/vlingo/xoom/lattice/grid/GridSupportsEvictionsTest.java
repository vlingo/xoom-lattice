// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.cluster.StaticClusterConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GridSupportsEvictionsTest {

  @Test
  public void testSupportsEvictions() throws Exception {
    final World world = World.startWithDefaults("test-world");

    StaticClusterConfiguration staticConfiguration = StaticClusterConfiguration.oneNode();

    final Grid grid = Grid.start(world, staticConfiguration.properties, staticConfiguration.propertiesOf(0));
    
    assertFalse(world.stage().supportsEvictions());
    assertTrue(grid.supportsEvictions());
  }
}
