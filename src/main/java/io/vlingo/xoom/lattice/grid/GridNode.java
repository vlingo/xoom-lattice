// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.cluster.model.application.ClusterApplicationAdapter;
import io.vlingo.xoom.cluster.model.attribute.Attribute;
import io.vlingo.xoom.cluster.model.attribute.AttributesProtocol;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.InboundGridActorControl.InboundGridActorControlInstantiator;
import io.vlingo.xoom.lattice.grid.application.*;
import io.vlingo.xoom.lattice.grid.attributes.AttributesSync;
import io.vlingo.xoom.lattice.grid.application.OutboundGridActorControl.OutboundGridActorControlInstantiator;
import io.vlingo.xoom.lattice.grid.application.message.GridDeliver;
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
import org.nustaq.serialization.FSTConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GridNode extends ClusterApplicationAdapter {
  // Sent messages waiting for continuation (answer) onto current node
  private static final Map<UUID, UnAckMessage> gridMessagesCorrelations = new ConcurrentHashMap<>();
  private static final Map<UUID, Returns<?>> actorMessagesCorrelations = new ConcurrentHashMap<>();

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
                                    gridMessagesCorrelations::put,
                                    actorMessagesCorrelations::put,
                                    new OutBuffers(holder)));

    this.gridRuntime.setOutbound(outbound);

    this.inbound =
            stage().actorFor(
                    GridActorControl.Inbound.class,
                    InboundGridActorControl.class,
                    new InboundGridActorControlInstantiator(
                            gridRuntime,
                            gridMessagesCorrelations::remove,
                            actorMessagesCorrelations::remove));

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
    outbound.informClusterIsHealthy(isHealthyCluster);
    gridRuntime.informAllLiveNodes(liveNodes);
    applicationMessageHandler.informClusterIsHealthy(isHealthyCluster);
  }

  @Override
  public void informNodeJoinedCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node joined: " + nodeId + " and is healthy: " + isHealthyCluster);
    outbound.informClusterIsHealthy(isHealthyCluster);
    gridRuntime.nodeJoined(nodeId);
    applicationMessageHandler.informClusterIsHealthy(isHealthyCluster);
  }

  @Override
  public void informNodeLeftCluster(final Id nodeId, final boolean isHealthyCluster) {
    logger().debug("GRID: Node left: " + nodeId + " and is healthy: " + isHealthyCluster);
    outbound.informClusterIsHealthy(isHealthyCluster);
    gridRuntime.hashRing().excludeNode(nodeId);
    applicationMessageHandler.informClusterIsHealthy(isHealthyCluster);
    retryUnAckMessagesOn(nodeId);
  }

  @Override
  public void informClusterIsHealthy(boolean isHealthyCluster) {
    logger().debug("GRID: Cluster is healthy: " + isHealthyCluster);
    outbound.informClusterIsHealthy(isHealthyCluster);
    if (isHealthyCluster) {
      quorumObservers.forEach(QuorumObserver::quorumAchieved);
    } else {
      quorumObservers.forEach(QuorumObserver::quorumLost);
    }
    applicationMessageHandler.informClusterIsHealthy(isHealthyCluster);
  }

  @Override
  public void informAttributesClient(final AttributesProtocol client) {
    logger().debug("GRID: Attributes Client received.");
    this.client = client;
    AttributesSync.instance(gridRuntime.localStage(), localNode.name().value(), client);
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
    final Map<UUID, UnAckMessage> retryMessages = gridMessagesCorrelations.entrySet().stream()
            .filter(entry -> leftNode.equals(entry.getValue().getReceiver()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    retryMessages.keySet()
            .forEach(gridMessagesCorrelations::remove);

    for (UnAckMessage retryMessage : retryMessages.values()) {
      GridDeliver<?> gridDeliver = retryMessage.getMessage();
      final Id newRecipient = gridRuntime.hashRing().nodeOf(gridDeliver.address.idString());

      if (newRecipient.equals(localNode.id())) {
        inbound.gridDeliver(newRecipient,
                newRecipient,
                retryMessage.getReturns(),
                (Class<Object>) gridDeliver.protocol,
                gridDeliver.address,
                gridDeliver.definition,
                (SerializableConsumer<Object>) gridDeliver.consumer,
                gridDeliver.representation);
      } else {
        outbound.gridDeliver(newRecipient,
                localNode.id(),
                retryMessage.getReturns(),
                (Class<Object>) gridDeliver.protocol,
                gridDeliver.address,
                gridDeliver.definition,
                (SerializableConsumer<Object>) gridDeliver.consumer,
                gridDeliver.representation);
      }
    }
  }
}
