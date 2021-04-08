package io.vlingo.xoom.lattice.grid.application;

public interface QuorumObserver {

  void quorumAchieved();
  void quorumLost();

}
