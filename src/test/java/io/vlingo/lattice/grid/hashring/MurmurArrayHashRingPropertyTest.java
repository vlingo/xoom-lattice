package io.vlingo.lattice.grid.hashring;

import java.util.function.BiFunction;

public class MurmurArrayHashRingPropertyTest extends HashRingPropertyTest {
  @Override
  protected HashRing<String> ring(
      final int pointsPerNode,
      final BiFunction<Integer, String, HashedNodePoint<String>> factory) {
    return new MurmurArrayHashRing<>(pointsPerNode, factory);
  }
}
