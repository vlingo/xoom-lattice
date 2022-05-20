package io.vlingo.xoom.lattice.grid.attributes;

import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.cluster.model.attribute.AttributesProtocol;

public interface AttributesSync {
  static AttributesSync instance(final Stage stage, final String nodeName, final AttributesProtocol client) {
    AttributesSyncInstantiator instantiator = new AttributesSyncInstantiator(nodeName, client);
    Definition definition = new Definition(AttributesSyncActor.class, instantiator, "attributes-sync-" + nodeName);

    return stage.actorFor(AttributesSync.class, definition);
  }

  class AttributesSyncInstantiator implements ActorInstantiator<AttributesSyncActor> {
    private final String nodeName;
    private final AttributesProtocol client;

    public AttributesSyncInstantiator(String nodeName, AttributesProtocol client) {
      this.nodeName = nodeName;
      this.client = client;
    }

    @Override
    public AttributesSyncActor instantiate() {
      return new AttributesSyncActor(nodeName, client);
    }

    @Override
    public Class<AttributesSyncActor> type() {
      return AttributesSyncActor.class;
    }
  }
}
