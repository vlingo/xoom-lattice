package io.vlingo.lattice.grid.example;

import io.vlingo.actors.StatelessGridActor;

public class PongerActor extends StatelessGridActor implements Ponger {
  private final Ponger self;

  public PongerActor() {
    self = selfAs(Ponger.class);
  }

  @Override
  public void pong(final Pinger pinger, final String node) {
    System.out.printf("Ponger::pong::%s::%s%n", node, address());
    try {
      Thread.sleep(1000);
      pinger.ping(self, node);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
