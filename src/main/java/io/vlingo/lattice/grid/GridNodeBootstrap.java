// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.actors.Logger;
import io.vlingo.actors.World;
import io.vlingo.cluster.model.Cluster;
import io.vlingo.cluster.model.ClusterSnapshotControl;
import io.vlingo.cluster.model.Properties;
import io.vlingo.cluster.model.application.ClusterApplication.ClusterApplicationInstantiator;
import io.vlingo.common.Tuple2;

public class GridNodeBootstrap {
  private static GridNodeBootstrap instance;

  private final Tuple2<ClusterSnapshotControl, Logger> clusterSnapshotControl;
  private final GridShutdownHook shutdownHook;

  public static GridNodeBootstrap boot(final String nodeName) throws Exception {
    final Grid grid = Grid.startWith("vlingo-lattice-grid", nodeName);
    return boot(grid.world(), grid, nodeName, false);
  }

  public static GridNodeBootstrap boot(final World world, final Grid grid, final String nodeName, final boolean embedded) throws Exception {
    final boolean mustBoot = GridNodeBootstrap.instance == null || !Cluster.isRunning();

    if (mustBoot) {
      Properties.instance.validateRequired(nodeName);

      final Tuple2<ClusterSnapshotControl, Logger> control =
              Cluster.controlFor(
                      world,
                      grid,
                      new GridNodeInstantiator(),
                      io.vlingo.cluster.model.Properties.instance,
                      nodeName);

      GridNodeBootstrap.instance = new GridNodeBootstrap(control, nodeName);

      control._2.info("Successfully started cluster node: '" + nodeName + "'");

      if (!embedded) {
        control._2.info("==========");
      }
    }

    return GridNodeBootstrap.instance;
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

  public ClusterSnapshotControl clusterSnapshotControl() {
    return clusterSnapshotControl._1;
  }

  private GridNodeBootstrap(final Tuple2<ClusterSnapshotControl, Logger> control, final String nodeName) throws Exception {
    this.clusterSnapshotControl = control;

    this.shutdownHook = new GridShutdownHook(nodeName, control);
    this.shutdownHook.register();
  }

  private static class GridNodeInstantiator extends ClusterApplicationInstantiator<GridNode> {
    private static final long serialVersionUID = -7260503652675037148L;

    public GridNodeInstantiator() {
      super(GridNode.class);
    }

    @Override
    public GridNode instantiate() {
      return new GridNode(node());
    }
  }
}
