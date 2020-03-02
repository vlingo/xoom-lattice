// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.actors.Grid;
import io.vlingo.actors.Logger;
import io.vlingo.actors.World;
import io.vlingo.cluster.model.Cluster;
import io.vlingo.cluster.model.ClusterSnapshotControl;
import io.vlingo.cluster.model.Properties;
import io.vlingo.cluster.model.application.ClusterApplication.ClusterApplicationInstantiator;
import io.vlingo.common.Tuple2;

public class GridNodeBootstrap {
  private static GridNodeBootstrap instance;

  private final GridShutdownHook shutdownHook;
  private final Grid grid;

  public static GridNodeBootstrap boot(final String nodeName) throws Exception {
    final Grid grid = Grid.start("vlingo-lattice-grid", nodeName);
    return boot(grid.world(), grid, nodeName, false);
  }

  public static GridNodeBootstrap boot(final World world, final Grid grid, final String nodeName, final boolean embedded) throws Exception {
    final boolean mustBoot = GridNodeBootstrap.instance == null || !Cluster.isRunning();

    if (mustBoot) {
      Properties.instance.validateRequired(nodeName);

      final Tuple2<ClusterSnapshotControl, Logger> control =
              Cluster.controlFor(
                      world,
                      new GridNodeInstantiator(grid),
                      io.vlingo.cluster.model.Properties.instance,
                      nodeName);

      GridNodeBootstrap.instance = new GridNodeBootstrap(control, nodeName, grid);

      control._2.info("Successfully started cluster node: '" + nodeName + "'");

      if (!embedded) {
        control._2.info("==========");
      }
    }

    return GridNodeBootstrap.instance;
  }

  public Grid grid() {
    return grid;
  }

  public static boolean exists() {
    return instance != null;
  }

  public static GridNodeBootstrap instance() {
    return instance;
  }

  public static void reset() {
    if (GridNodeBootstrap.instance != null) {
      Cluster.reset();
      GridNodeBootstrap.instance = null;
    }
  }

  private GridNodeBootstrap(final Tuple2<ClusterSnapshotControl, Logger> control, final String nodeName, Grid grid) throws Exception {
    this.grid = grid;

    this.shutdownHook = new GridShutdownHook(nodeName, control);
    this.shutdownHook.register();
  }

  private static class GridNodeInstantiator extends ClusterApplicationInstantiator<GridNode> {

    private static final long serialVersionUID = -7096922857258549619L;

    private final Grid grid;

    public GridNodeInstantiator(Grid grid) {
      super(GridNode.class);
      this.grid = grid;
    }

    @Override
    public GridNode instantiate() {
      grid.setNodeId(node().id());
      return new GridNode(grid, node());
    }
  }
}
