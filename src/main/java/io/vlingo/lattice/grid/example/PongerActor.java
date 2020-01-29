package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Actor;

public class PongerActor extends Actor implements Ponger {
  private final Ponger self;

  public PongerActor() {
    self = selfAs(Ponger.class);
  }

  @Override
  public void pong(final Pinger pinger) {
    System.out.println("Ponger::pong");
    try {
      Thread.sleep(1000);
      pinger.ping(self);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
