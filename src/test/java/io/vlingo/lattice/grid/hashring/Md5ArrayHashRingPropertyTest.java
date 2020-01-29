package io.vlingo.lattice.grid.hashring;

import java.util.function.BiFunction;

public class Md5ArrayHashRingPropertyTest extends HashRingPropertyTest {
  @Override
  protected HashRing<String> ring(
      final int pointsPerNode,
      final BiFunction<Integer, String, HashedNodePoint<String>> factory)
      throws Exception {
    return new MD5ArrayHashRing<>(pointsPerNode, factory);
  }
}
