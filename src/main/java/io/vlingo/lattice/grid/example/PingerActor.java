package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Actor;

public class PingerActor extends Actor implements Pinger {
  private final Pinger self;

  public PingerActor() {
    self = selfAs(Pinger.class);
  }

  @Override
  public void ping(final Ponger ponger, String node) {
    System.out.printf("Pinger::ping::%s%n", node);
    try {
      Thread.sleep(1000);
      ponger.pong(self, node);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
