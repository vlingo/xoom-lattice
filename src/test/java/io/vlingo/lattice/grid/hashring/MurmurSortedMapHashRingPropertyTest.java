package io.vlingo.lattice.grid.hashring;

import java.util.function.BiFunction;

public class MurmurSortedMapHashRingPropertyTest extends HashRingPropertyTest {
  @Override
  protected HashRing<String> ring(
      final int pointsPerNode,
      final BiFunction<Integer, String, HashedNodePoint<String>> factory) {
    return new MurmurSortedMapHashRing<>(pointsPerNode);
  }
}
