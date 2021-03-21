// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.cluster.ClusterProperties;
import io.vlingo.cluster.model.Properties;

/**
 * Properties that are predefined for single-node and three-node grid clusters. These
 * are useful for development and test, but provided in {@code java/main} for access
 * by tests outside of {@code vlingo-lattice} and {@code vlingo-cluster}.
 */
public class GridClusterPropertiesTest {

  @Test
  public void shouldConfigureThreeNodeGrid() {
    final Properties properties = GridClusterProperties.allNodes();

    // common (other common properties are tested in vlingo-cluster

    final String[] seedNodes = properties.getString("cluster.seedNodes", "").split(",");

    assertEquals(3, seedNodes.length);
    assertEquals("node1", seedNodes[0]);
    assertEquals("node2", seedNodes[1]);
    assertEquals("node3", seedNodes[2]);

    // node specific
    assertEquals("1", properties.getString("node.node1.id", ""));
    assertEquals("node1", properties.getString("node.node1.name", ""));
    assertEquals("localhost", properties.getString("node.node1.host", ""));

    assertEquals("2", properties.getString("node.node2.id", ""));
    assertEquals("node2", properties.getString("node.node2.name", ""));
    assertEquals("localhost", properties.getString("node.node2.host", ""));

    assertEquals("3", properties.getString("node.node3.id", ""));
    assertEquals("node3", properties.getString("node.node3.name", ""));
    assertEquals("localhost", properties.getString("node.node3.host", ""));

    assertEquals("", properties.getString("node.node4.id", ""));
    assertEquals("", properties.getString("node.node4.name", ""));
    assertEquals("", properties.getString("node.node4.host", ""));
  }

  @Test
  public void shouldConfigureSingleNodeGrid() {
    final Properties properties = ClusterProperties.oneNode();

    // common (other common properties are tested in vlingo-cluster

    final String[] seedNodes = properties.getString("cluster.seedNodes", "").split(",");

    assertEquals(1, seedNodes.length);
    assertEquals("node1", seedNodes[0]);

    // node specific
    assertEquals("1", properties.getString("node.node1.id", ""));
    assertEquals("node1", properties.getString("node.node1.name", ""));
    assertEquals("localhost", properties.getString("node.node1.host", ""));

    assertEquals("", properties.getString("node.node2.id", ""));
    assertEquals("", properties.getString("node.node2.name", ""));
    assertEquals("", properties.getString("node.node2.host", ""));
  }
}