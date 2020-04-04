package io.vlingo.actors;

public interface GridState {
  void quorumAchieved();
  void quorumLost();

  boolean hasQuorum();
}
