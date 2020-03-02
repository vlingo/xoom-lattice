package io.vlingo.lattice.grid.example;

import io.vlingo.actors.StatelessGridActor;

public class PingerActor extends StatelessGridActor implements Pinger {
  private Pinger self;

  public PingerActor() {
    self = selfAs(Pinger.class);
  }

  @Override
  public void ping(final Ponger ponger, String node) {
    System.out.printf("Pinger::ping::%s::%s%n", node, address());
    try {
      Thread.sleep(1000);
      ponger.pong(self, node);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
