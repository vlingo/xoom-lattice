package io.vlingo.xoom.lattice.grid.attributes;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.cluster.model.attribute.Attribute;
import io.vlingo.xoom.cluster.model.attribute.AttributesProtocol;
import io.vlingo.xoom.common.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class AttributesSyncActor extends Actor implements AttributesSync, Scheduled<Object> {
  private static final String attributesSet = "attributes-sync";

  private final String nodeName;
  private final AttributesProtocol client;

  public AttributesSyncActor(String nodeName, AttributesProtocol client) {
    this.nodeName = nodeName;
    this.client = client;

    this.client.add(attributesSet, nodeName, "UNDEFINED");
    scheduler().schedule(selfAs(Scheduled.class), null, 5000L, 5000L);
  }

  @Override
  public void intervalSignal(Scheduled<Object> scheduled, Object o) {
    String value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    client.replace(attributesSet, nodeName, value);

    Collection<Attribute<?>> all = client.allOf(attributesSet);
    all.forEach(attr -> logger().info("--> " + attr));
  }
}
