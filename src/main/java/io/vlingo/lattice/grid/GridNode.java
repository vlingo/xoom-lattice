// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import java.util.Collection;

import io.vlingo.cluster.model.application.ClusterApplicationAdapter;
import io.vlingo.cluster.model.attribute.Attribute;
import io.vlingo.cluster.model.attribute.AttributesProtocol;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;
import io.vlingo.wire.node.Node;

public class GridNode extends ClusterApplicationAdapter {
  private AttributesProtocol client;
  private final Node localNode;

  public GridNode(final Node localNode) {
    this.localNode = localNode;
  }

  @Override
  public void start() {
    logger().debug("GRID: Started on node: " + localNode);
  }

  @Override
  public void handleApplicationMessage(final RawMessage message, final ApplicationOutboundStream responder) {
    logger().debug("GRID: Received application message: " + message.asTextMessage());
  }

  @Override
  public void informAllLiveNodes(final Collection<Node> liveNodes, final boolean isHealthyCluster) {
    logger().debug("GRID: Live nodes confirmed: " + liveNodes + " and is healthy: " + isHealthyCluster);
  }

  @Override
  public void informLeaderElected(final Id leaderId, final boolean isHealthyCluster, final boolean isLocalNodeLeading) {
    logger().debug("GRID: Leader elected: " + leaderId + " and is healthy: " + isHealthyCluster);

    if (isLocalNodeLeading) {
      logger().debug("GRID: Local node is leading.");
   }
  }

  @Override
  public void informLeaderLost(final Id lostLeaderId, final boolean isHealthyCluster) {
    logger().debug("GRID: Leader lost: " + lostLeaderId + " and is healthy: " + isHealthyCluster);
  }

  @Override
  public void informLocalNodeShutDown(final Id nodeId) {
    logger().debug("GRID: Local node shut down: " + nodeId);
  }

  @Override
  public void informLocalNodeStarted(final Id nodeId) {
    logger().debug("GRID: Local node started: " + nodeId);
  }

  @Override
  public void informNodeIsHealthy(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node reported healthy: " + nodeId + " and is healthy: " + isHealthyCluster);
  }

  @Override
  public void informNodeJoinedCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node joined: " + nodeId + " and is healthy: " + isHealthyCluster);
  }

  @Override
  public void informNodeLeftCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node left: " + nodeId + " and is healthy: " + isHealthyCluster);
  }

  @Override
  public void informQuorumAchieved() {
    logger().debug("GRID: Quorum achieved");
  }

  @Override
  public void informQuorumLost() {
    logger().debug("GRID: Quorum lost");
  }

  @Override
  public void informAttributesClient(final AttributesProtocol client) {
    logger().debug("GRID: Attributes Client received.");
    this.client = client;
  }

  @Override
  public void informAttributeSetCreated(final String attributeSetName) {
     logger().debug("GRID: Attributes Set Created: " + attributeSetName);
  }

  @Override
  public void informAttributeAdded(final String attributeSetName, final String attributeName) {
    final Attribute<String> attr = client.attribute(attributeSetName, attributeName);
    logger().debug("GRID: Attribute Set " + attributeSetName + " Attribute Added: " + attributeName + " Value: " + attr.value);
  }

  @Override
  public void informAttributeRemoved(final String attributeSetName, final String attributeName) {
    final Attribute<String> attr = client.attribute(attributeSetName, attributeName);
    logger().debug("GRID: Attribute Set " + attributeSetName + " Attribute Removed: " + attributeName + " Attribute: " + attr);
  }

  @Override
  public void informAttributeSetRemoved(final String attributeSetName) {
    logger().debug("GRID: Attributes Set Removed: " + attributeSetName);
  }

  @Override
  public void informAttributeReplaced(final String attributeSetName, final String attributeName) {
    final Attribute<String> attr = client.attribute(attributeSetName, attributeName);
    logger().debug("GRID: Attribute Set " + attributeSetName + " Attribute Replaced: " + attributeName + " Value: " + attr.value);
  }
}
