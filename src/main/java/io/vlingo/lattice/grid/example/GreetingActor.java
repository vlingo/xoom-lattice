package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Actor;

public class GreetingActor extends Actor implements Greeting {

  private final Greeting self;

  public GreetingActor() {
    self = selfAs(Greeting.class);
  }

  @Override
  public void hello(final String name) {
    logger().info("Hello {}", name);
  }
}
