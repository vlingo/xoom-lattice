package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Definition;
import io.vlingo.common.BasicCompletes;
import io.vlingo.common.Completes;

import java.util.Collections;

public class GreetingActor extends Actor implements Greeting {

  private final String name;

  public GreetingActor(String name) {
    this.name = name;
  }

  @Override
  public void hello(final String from) {
    logger().info("Hello {}", from);
  }

  @Override
  public Completes<Pinger> respond(String from) {
    logger().info("Responding from {}", from);
    Pinger pinger = childActorFor(Pinger.class, Definition.has(PingerActor.class, Collections.emptyList()));
    return completes().with(pinger);
  }
}
