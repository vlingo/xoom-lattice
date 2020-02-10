package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Actor;
import io.vlingo.common.BasicCompletes;
import io.vlingo.common.Completes;

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
  public Completes<String> respond(String from) {
    return completes().with(
        String.format("Hello %s from %s", from, name));
  }
}
