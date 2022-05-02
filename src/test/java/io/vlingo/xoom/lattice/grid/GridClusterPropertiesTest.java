// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.cluster.model.Properties;

/**
 * Properties that are predefined for single-node and three-node grid clusters. These
 * are useful for development and test, but provided in {@code java/main} for access
 * by tests outside of {@code xoom-lattice} and {@code xoom-cluster}.
 */
public class GridClusterPropertiesTest {

  @Test
  public void shouldConfigureThreeNodeGrid() {
    final Properties properties = GridClusterProperties.allNodes();

    // common (other common properties are tested in xoom-cluster

    assertEquals(GridNode.class.getName(), properties.getString("cluster.app.class", ""));

    final String[] seedNodes = properties.getString("cluster.seedNodes", "").split(",");

    assertEquals(3, seedNodes.length);
    assertEquals("node1", seedNodes[0]);
    assertEquals("node2", seedNodes[1]);
    assertEquals("node3", seedNodes[2]);

    // node specific
    assertEquals("1", properties.getString("node.node1.id", ""));
    assertEquals("node1", properties.getString("node.node1.name", ""));
    assertEquals("localhost", properties.getString("node.node1.host", ""));
    assertEquals("37371", properties.getString("node.node1.op.port", ""));
    assertEquals("37372", properties.getString("node.node1.app.port", ""));

    assertEquals("2", properties.getString("node.node2.id", ""));
    assertEquals("node2", properties.getString("node.node2.name", ""));
    assertEquals("localhost", properties.getString("node.node2.host", ""));
    assertEquals("37373", properties.getString("node.node2.op.port", ""));
    assertEquals("37374", properties.getString("node.node2.app.port", ""));

    assertEquals("3", properties.getString("node.node3.id", ""));
    assertEquals("node3", properties.getString("node.node3.name", ""));
    assertEquals("localhost", properties.getString("node.node3.host", ""));
    assertEquals("37375", properties.getString("node.node3.op.port", ""));
    assertEquals("37376", properties.getString("node.node3.app.port", ""));

    assertEquals("", properties.getString("node.node4.id", ""));
    assertEquals("", properties.getString("node.node4.name", ""));
    assertEquals("", properties.getString("node.node4.host", ""));
  }

  @Test
  public void shouldConfigureSingleNodeGrid() {
    final Properties properties = GridClusterProperties.oneNode();

    // common (other common properties are tested in xoom-cluster

    assertEquals(GridNode.class.getName(), properties.getString("cluster.app.class", ""));

    final String[] seedNodes = properties.getString("cluster.seedNodes", "").split(",");

    assertEquals(1, seedNodes.length);
    assertEquals("node1", seedNodes[0]);

    // node specific
    assertEquals("1", properties.getString("node.node1.id", ""));
    assertEquals("node1", properties.getString("node.node1.name", ""));
    assertEquals("localhost", properties.getString("node.node1.host", ""));
    assertEquals("37371", properties.getString("node.node1.op.port", ""));
    assertEquals("37372", properties.getString("node.node1.app.port", ""));

    assertEquals("", properties.getString("node.node2.id", ""));
    assertEquals("", properties.getString("node.node2.name", ""));
    assertEquals("", properties.getString("node.node2.host", ""));
  }
}
