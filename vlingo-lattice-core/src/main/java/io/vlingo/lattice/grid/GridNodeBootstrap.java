// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.actors.Logger;
import io.vlingo.cluster.model.Cluster;
import io.vlingo.cluster.model.ClusterSnapshotControl;
import io.vlingo.cluster.model.Properties;
import io.vlingo.common.Tuple2;

public class GridNodeBootstrap {
  private static GridNodeBootstrap instance;

  private final Tuple2<ClusterSnapshotControl, Logger> clusterSnapshotControl;
  private final GridShutdownHook shutdownHook;

  public static GridNodeBootstrap boot(final String nodeName) throws Exception {
    return boot(nodeName, false);
  }

  public static GridNodeBootstrap boot(final String nodeName, final boolean embedded) throws Exception {
    final boolean mustBoot = GridNodeBootstrap.instance == null || !Cluster.isRunning();

    if (mustBoot) {
      Properties.instance.validateRequired(nodeName);

      final Tuple2<ClusterSnapshotControl, Logger> control = Cluster.controlFor(nodeName);

      GridNodeBootstrap.instance = new GridNodeBootstrap(control, nodeName);

      control._2.info("Successfully started cluster node: '" + nodeName + "'");

      if (!embedded) {
        control._2.info("==========");
      }
    }

    return GridNodeBootstrap.instance;
  }

  public static GridNodeBootstrap instance() {
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
}
