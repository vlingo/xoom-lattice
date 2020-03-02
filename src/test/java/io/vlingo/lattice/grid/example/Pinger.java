package io.vlingo.lattice.grid.example;

public interface Pinger {
  void ping(final Ponger ponger, final String node);
}
