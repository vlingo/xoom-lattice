// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.nustaq.serialization.FSTConfiguration;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.cluster.model.application.ClusterApplicationAdapter;
import io.vlingo.xoom.cluster.model.attribute.Attribute;
import io.vlingo.xoom.cluster.model.attribute.AttributesProtocol;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.InboundGridActorControl.InboundGridActorControlInstantiator;
import io.vlingo.xoom.lattice.grid.application.ApplicationMessageHandler;
import io.vlingo.xoom.lattice.grid.application.GridActorControl;
import io.vlingo.xoom.lattice.grid.application.GridApplicationMessageHandler;
import io.vlingo.xoom.lattice.grid.application.OutboundGridActorControl;
import io.vlingo.xoom.lattice.grid.application.OutboundGridActorControl.OutboundGridActorControlInstantiator;
import io.vlingo.xoom.lattice.grid.application.QuorumObserver;
import io.vlingo.xoom.lattice.grid.application.message.Deliver;
import io.vlingo.xoom.lattice.grid.application.message.UnAckMessage;
import io.vlingo.xoom.lattice.grid.application.message.serialization.FSTDecoder;
import io.vlingo.xoom.lattice.grid.application.message.serialization.FSTEncoder;
import io.vlingo.xoom.lattice.util.ExpiringHardRefHolder;
import io.vlingo.xoom.lattice.util.HardRefHolder;
import io.vlingo.xoom.lattice.util.OutBuffers;
import io.vlingo.xoom.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.xoom.wire.message.RawMessage;
import io.vlingo.xoom.wire.node.Id;
import io.vlingo.xoom.wire.node.Node;

public class GridNode extends ClusterApplicationAdapter {
  // Sent messages waiting for continuation (answer) onto current node
  private static final Map<UUID, UnAckMessage> correlationMessages = new ConcurrentHashMap<>();

  private AttributesProtocol client;
  private final GridRuntime gridRuntime;
  private final Node localNode;

  private final GridActorControl.Outbound outbound;

  private final GridActorControl.Inbound inbound;

  private final ApplicationMessageHandler applicationMessageHandler;

  private final Collection<QuorumObserver> quorumObservers;

  public GridNode(final GridRuntime gridRuntime, final Node localNode) {
    this.gridRuntime = gridRuntime;
    this.localNode = localNode;

    final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    // set classloader with available proxy classes
    conf.setClassLoader(gridRuntime.worldClassLoader());

    final HardRefHolder holder = gridRuntime.world().actorFor(HardRefHolder.class,
        Definition.has(ExpiringHardRefHolder.class, ExpiringHardRefHolder::new));

    this.outbound =
            stage().actorFor(
                    GridActorControl.Outbound.class,
                    OutboundGridActorControl.class,
                    new OutboundGridActorControlInstantiator(
                                    localNode.id(),
                                    new FSTEncoder(conf),
                                    correlationMessages::put,
                                    new OutBuffers(holder)));

    this.gridRuntime.setOutbound(outbound);

    this.inbound =
            stage().actorFor(
                    GridActorControl.Inbound.class,
                    InboundGridActorControl.class,
                    new InboundGridActorControlInstantiator(
                            gridRuntime,
                            correlationMessages::remove));

    this.applicationMessageHandler =
            new GridApplicationMessageHandler(
                    localNode.id(),
                    gridRuntime.hashRing(),
                    inbound,
                    outbound,
                    new FSTDecoder(conf), holder,
                    scheduler());

    this.quorumObservers = new ArrayList<>(3);

    registerQuorumObserver(gridRuntime);
  }

  public final void registerQuorumObserver(final QuorumObserver observer) {
    this.quorumObservers.add(observer);
  }

  @Override
  public void start() {
    logger().debug("GRID: Started on node: " + localNode);
    gridRuntime.hashRing().includeNode(localNode.id());
  }

  @Override
  public void handleApplicationMessage(final RawMessage message) {
    logger().debug("GRID: Received application message: " + message.asTextMessage());
    applicationMessageHandler.handle(message);
  }

  @Override
  public void informResponder(ApplicationOutboundStream responder) {
    this.outbound.useStream(responder);
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
    // TODO relocate local actors to another node?
  }

  @Override
  public void informLocalNodeStarted(final Id nodeId) {
    logger().debug("GRID: Local node started: " + nodeId);
  }

  @Override
  public void informNodeIsHealthy(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node reported healthy: " + nodeId + " and is healthy: " + isHealthyCluster);
    if (isHealthyCluster) {
      outbound.disburse(nodeId);
      applicationMessageHandler.disburse(nodeId);
    }
  }

  @Override
  public void informNodeJoinedCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node joined: " + nodeId + " and is healthy: " + isHealthyCluster);
    gridRuntime.nodeJoined(nodeId);
  }

  @Override
  public void informNodeLeftCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node left: " + nodeId + " and is healthy: " + isHealthyCluster);
    gridRuntime.hashRing().excludeNode(nodeId);
    retryUnAckMessagesOn(nodeId);
  }

  @Override
  public void informQuorumAchieved() {
    logger().debug("GRID: Quorum achieved");
    quorumObservers.forEach(QuorumObserver::quorumAchieved);
  }

  @Override
  public void informQuorumLost() {
    logger().debug("GRID: Quorum lost");
    quorumObservers.forEach(QuorumObserver::quorumLost);
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

  @Override
  public void stop() {
    if (!isStopped()) {
      logger().debug("GRID: Stopping...");
      gridRuntime.relocateActors();
      super.stop();
    }
  }

  /**
   * Retry unacknowledged messages onto a new node (recipient).
   *
   * @param leftNode The node that left the cluster
   */
  @SuppressWarnings("unchecked")
  private void retryUnAckMessagesOn(final Id leftNode) {
    final Map<UUID, UnAckMessage> retryMessages = correlationMessages.entrySet().stream()
            .filter(entry -> leftNode.equals(entry.getValue().getReceiver()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    retryMessages.keySet().stream()
            .forEach(correlationMessages::remove);

    for (UnAckMessage retryMessage : retryMessages.values()) {
      Deliver<?> deliver = retryMessage.getMessage();
      final Id newRecipient = gridRuntime.hashRing().nodeOf(deliver.address.idString());

      if (newRecipient.equals(localNode)) {
        inbound.deliver(newRecipient,
                newRecipient,
                retryMessage.getReturns(),
                (Class<Object>) deliver.protocol,
                deliver.address,
                deliver.definition,
                (SerializableConsumer<Object>) deliver.consumer,
                deliver.representation);
      } else {
        outbound.deliver(newRecipient,
                localNode.id(),
                retryMessage.getReturns(),
                (Class<Object>) deliver.protocol,
                deliver.address,
                deliver.definition,
                (SerializableConsumer<Object>) deliver.consumer,
                deliver.representation);
      }
    }
  }
}
