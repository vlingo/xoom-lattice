// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.actors.GridRuntime;
import io.vlingo.actors.Logger;
import io.vlingo.cluster.model.Cluster;
import io.vlingo.cluster.model.ClusterSnapshotControl;
import io.vlingo.cluster.model.application.ClusterApplication.ClusterApplicationInstantiator;
import io.vlingo.common.Tuple2;

public class GridNodeBootstrap {
  private final Tuple2<ClusterSnapshotControl, Logger> clusterSnapshotControl;

  private final GridShutdownHook shutdownHook;

  public static GridNodeBootstrap boot(final GridRuntime grid, final String nodeName, final boolean embedded) throws Exception {
    return boot(grid, nodeName, io.vlingo.cluster.model.Properties.instance, embedded);
  }

  public static GridNodeBootstrap boot(final GridRuntime grid, final String nodeName, final io.vlingo.cluster.model.Properties properties, final boolean embedded) throws Exception {
    properties.validateRequired(nodeName);

    final Tuple2<ClusterSnapshotControl, Logger> control =
            Cluster.controlFor(
                    grid.world(),
                    new GridNodeInstantiator(grid),
                    properties,
                    nodeName);

    final GridNodeBootstrap instance = new GridNodeBootstrap(control, nodeName);

    control._2.info("Successfully started cluster node: '" + nodeName + "'");

    if (!embedded) {
      control._2.info("==========");
    }

    return instance;
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

    private static final long serialVersionUID = -7096922857258549619L;

    private final GridRuntime gridRuntime;

    public GridNodeInstantiator(final GridRuntime gridRuntime) {
      super(GridNode.class);
      this.gridRuntime = gridRuntime;
    }

    @Override
    public GridNode instantiate() {
      gridRuntime.setNodeId(node().id());
      return new GridNode(gridRuntime, node());
    }
  }
}
