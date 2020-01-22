package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Actor;

public class PingerActor extends Actor implements Pinger {
  private final Pinger self;

  public PingerActor() {
    self = selfAs(Pinger.class);
  }

  public void ping(final Ponger ponger) {
    System.out.println("Pinger::ping");
    try {
      Thread.sleep(1000);
      ponger.pong(self);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
