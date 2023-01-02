// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import io.vlingo.xoom.cluster.StaticClusterConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

public class GridClusterProperties {
  private static final int DefaultStartPort = 37371;
  private static final int DefaultTotalNodes = 3;

  public static StaticClusterConfiguration allNodes() {
    return allNodes(DefaultStartPort);
  }

  public static StaticClusterConfiguration allNodes(final int startPort) {
    return allNodes(startPort, DefaultTotalNodes);
  }

  public static StaticClusterConfiguration allNodes(final int startPort, final int totalNodes) {
    return StaticClusterConfiguration.allNodes(new AtomicInteger(startPort - 1), totalNodes, GridNode.class.getName());
  }

  public static StaticClusterConfiguration oneNode() {
    return oneNode(DefaultStartPort);
  }

  public static StaticClusterConfiguration oneNode(final int startPort) {
    return StaticClusterConfiguration.oneNode(new AtomicInteger(startPort - 1), GridNode.class.getName());
  }
}
