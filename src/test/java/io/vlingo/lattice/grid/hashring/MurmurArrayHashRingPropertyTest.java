package io.vlingo.lattice.grid.hashring;

import org.junit.Ignore;

import java.util.function.BiFunction;

@Ignore
public class MurmurArrayHashRingPropertyTest extends HashRingPropertyTest {
  @Override
  protected HashRing<String> ring(
      final int pointsPerNode,
      final BiFunction<Integer, String, HashedNodePoint<String>> factory) {
    return new MurmurArrayHashRing<>(pointsPerNode, factory);
  }
}
