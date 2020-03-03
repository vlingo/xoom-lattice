// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nustaq.serialization.FSTConfiguration;

import io.vlingo.actors.Grid;
import io.vlingo.actors.InboundGridActorControl;
import io.vlingo.actors.Returns;
import io.vlingo.cluster.model.application.ClusterApplicationAdapter;
import io.vlingo.cluster.model.attribute.Attribute;
import io.vlingo.cluster.model.attribute.AttributesProtocol;
import io.vlingo.lattice.grid.application.ApplicationMessageHandler;
import io.vlingo.lattice.grid.application.GridApplicationMessageHandler;
import io.vlingo.lattice.grid.application.OutboundGridActorControl;
import io.vlingo.lattice.grid.application.message.serialization.FSTDecoder;
import io.vlingo.lattice.grid.application.message.serialization.FSTEncoder;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;
import io.vlingo.wire.node.Node;

public class GridNode extends ClusterApplicationAdapter {

  private static final FSTConfiguration CONF = FSTConfiguration.createDefaultConfiguration();
  private static final Map<UUID, Returns<?>> correlation = new HashMap<>();

  private AttributesProtocol client;
  private final Grid grid;
  private final Node localNode;

  private final OutboundGridActorControl outbound;

  final ApplicationMessageHandler applicationMessageHandler;

  public GridNode(final Grid grid, final Node localNode) {
    this.grid = grid;
    this.localNode = localNode;
    this.outbound = new OutboundGridActorControl(localNode.id(), new FSTEncoder(CONF), correlation::put);
    this.grid.setOutbound(outbound);
    final InboundGridActorControl inbound = new InboundGridActorControl(logger(), grid, correlation::remove);
    this.applicationMessageHandler = new GridApplicationMessageHandler(
        localNode.id(), grid.hashRing(), inbound, outbound, new FSTDecoder(CONF));
  }

  @Override
  public void start() {
    logger().debug("GRID: Started on node: " + localNode);
    grid.hashRing().includeNode(localNode.id());
  }

  @Override
  public void handleApplicationMessage(final RawMessage message) {
    logger().debug("GRID: Received application message: " + message.asTextMessage());
    applicationMessageHandler.handle(message);
  }

  @Override
  public void informResponder(ApplicationOutboundStream responder) {
    this.outbound.setStream(responder);
  }

  // hashring ??? THIS ONE ONLY?
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

  // hashring ???
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

  // hashring
  @Override
  public void informNodeJoinedCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node joined: " + nodeId + " and is healthy: " + isHealthyCluster);
    grid.nodeJoined(nodeId);
  }

  // hashring
  @Override
  public void informNodeLeftCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node left: " + nodeId + " and is healthy: " + isHealthyCluster);
    grid.hashRing().excludeNode(nodeId);
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
