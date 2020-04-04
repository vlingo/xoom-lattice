package io.vlingo.lattice.grid.application;

public interface QuorumObserver {

  void quorumAchieved();
  void quorumLost();

}
