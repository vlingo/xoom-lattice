// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import static org.junit.Assert.assertEquals;

import io.vlingo.xoom.cluster.StaticClusterConfiguration;
import io.vlingo.xoom.cluster.model.application.FakeClusterApplicationActor;
import io.vlingo.xoom.wire.node.Node;
import org.junit.Test;

import io.vlingo.xoom.cluster.model.Properties;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Properties that are predefined for single-node and three-node grid clusters. These
 * are useful for development and test, but provided in {@code java/main} for access
 * by tests outside of {@code xoom-lattice} and {@code xoom-cluster}.
 */
public class GridClusterPropertiesTest {

  @Test
  public void shouldConfigureThreeNodeGrid() {
    final int portSeed = 10_000 + new Random().nextInt(50_000);
    final StaticClusterConfiguration staticConfiguration = StaticClusterConfiguration.allNodes(new AtomicInteger(portSeed));
    final Properties properties = staticConfiguration.properties;

    // common (other common properties are tested in xoom-cluster

    assertEquals(FakeClusterApplicationActor.class.getName(), properties.getString("cluster.app.class", ""));

    final String[] seeds = properties.getString("cluster.seeds", "").split(",");
    assertEquals(1, seeds.length);

    assertEquals(false, staticConfiguration.properties.singleNode());
    assertEquals(3, staticConfiguration.allNodes.size());

    // node specific
    final Node node1 = staticConfiguration.allNodes.get(0);
    assertEquals(1, node1.id().value());
    assertEquals("node1", node1.name().value());
    assertEquals(true, node1.isSeed());
    assertEquals("localhost", node1.operationalAddress().hostName());
    assertEquals(portSeed + 1, node1.operationalAddress().port());
    assertEquals("localhost", node1.applicationAddress().hostName());
    assertEquals(portSeed + 2, node1.applicationAddress().port());

    final Node node2 = staticConfiguration.allNodes.get(1);
    assertEquals(2, node2.id().value());
    assertEquals("node2", node2.name().value());
    assertEquals(false, node2.isSeed());
    assertEquals("localhost", node2.operationalAddress().hostName());
    assertEquals(portSeed + 3, node2.operationalAddress().port());
    assertEquals("localhost", node2.applicationAddress().hostName());
    assertEquals(portSeed + 4, node2.applicationAddress().port());

    final Node node3 = staticConfiguration.allNodes.get(2);
    assertEquals(3, node3.id().value());
    assertEquals("node3", node3.name().value());
    assertEquals(false, node3.isSeed());
    assertEquals("localhost", node3.operationalAddress().hostName());
    assertEquals(portSeed + 5, node3.operationalAddress().port());
    assertEquals("localhost", node3.applicationAddress().hostName());
    assertEquals(portSeed + 6, node3.applicationAddress().port());
  }

  @Test
  public void shouldConfigureSingleNodeGrid() {
    final int portSeed = 10_000 + new Random().nextInt(50_000);
    final StaticClusterConfiguration staticConfiguration = StaticClusterConfiguration.oneNode(new AtomicInteger(portSeed));
    final Properties properties = staticConfiguration.properties;

    // common (other common properties are tested in xoom-cluster

    assertEquals(FakeClusterApplicationActor.class.getName(), properties.getString("cluster.app.class", ""));
    assertEquals("", properties.getString("cluster.seeds", "")); // single node has no 'cluster.seeds'

    assertEquals(1, staticConfiguration.allNodes.size());
    assertEquals(true, staticConfiguration.properties.singleNode());

    // node specific
    final Node node1 = staticConfiguration.allNodes.get(0);
    assertEquals(1, node1.id().value());
    assertEquals("node1", node1.name().value());
    assertEquals("localhost", node1.operationalAddress().hostName());
    assertEquals(portSeed + 1, node1.operationalAddress().port());
    assertEquals("localhost", node1.applicationAddress().hostName());
    assertEquals(portSeed + 2, node1.applicationAddress().port());
  }
}
