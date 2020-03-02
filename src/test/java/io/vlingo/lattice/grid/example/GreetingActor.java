package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Definition;
import io.vlingo.actors.GridActor;
import io.vlingo.common.Completes;

import java.io.Serializable;
import java.util.Collections;

public class GreetingActor extends GridActor<GreetingActor.Proxy> implements Greeting {

  private String name;

  public GreetingActor(String name) {
    this.name = name;
  }

  public GreetingActor() {
    this.name = null;
  }

  @Override
  public void hello(final String from) {
    logger().info("Hello {}", from);
  }

  @Override
  public Completes<Pinger> respond(String from) {
    logger().info("Responding from {} at {}", from, address());
    Pinger pinger = childActorFor(Pinger.class, Definition.has(PingerActor.class, Collections.emptyList()));
    return completes().with(pinger);
  }

  @Override
  public Proxy provideRelocationSnapshot() {
    return new Proxy(name);
  }

  @Override
  public void applyRelocationSnapshot(Proxy snapshot) {
    this.name = snapshot.name;
  }

  public static final class Proxy implements Serializable {
    private static final long serialVersionUID = -2796142731077588067L;

    final String name;

    public Proxy(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return String.format("Proxy(name='%s')", name);
    }
  }
}
