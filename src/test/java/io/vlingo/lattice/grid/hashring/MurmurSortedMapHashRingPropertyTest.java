package io.vlingo.lattice.grid.hashring;

import org.junit.Test;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class MurmurSortedMapHashRingPropertyTest extends HashRingPropertyTest {
  @Override
  protected HashRing<String> ring(
      final int pointsPerNode,
      final BiFunction<Integer, String, HashedNodePoint<String>> factory) {
    return new MurmurSortedMapHashRing<>(pointsPerNode);
  }
}
