// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.cluster.ClusterProperties;

public class GridClusterProperties {
  private static final int DefaultStartPort = 37371;
  private static final int DefaultTotalNodes = 3;

  public static io.vlingo.xoom.cluster.model.Properties allNodes() {
    return allNodes(DefaultStartPort);
  }

  public static io.vlingo.xoom.cluster.model.Properties allNodes(final int startPort) {
    return allNodes(startPort, DefaultTotalNodes);
  }

  public static io.vlingo.xoom.cluster.model.Properties allNodes(final int startPort, final int totalNodes) {
    return ClusterProperties.allNodes(new AtomicInteger(startPort - 1), totalNodes, GridNode.class.getName());
  }

  public static io.vlingo.xoom.cluster.model.Properties oneNode() {
    return oneNode(DefaultStartPort);
  }

  public static io.vlingo.xoom.cluster.model.Properties oneNode(final int startPort) {
    return ClusterProperties.oneNode(new AtomicInteger(startPort - 1), GridNode.class.getName());
  }
}
