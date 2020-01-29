package io.vlingo.lattice.grid.hashring;

import io.vlingo.common.Tuple2;
import org.junit.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public abstract class HashRingPropertyTest {

  private static final int POINTS_PER_NODE = 100;
  private static final BiFunction<Integer, String, HashedNodePoint<String>> FACTORY =
      (hash, node) -> new HashedNodePoint<String>(hash, node) {
        @Override
        public void excluded() {
        }

        @Override
        public void included() {
        }
      };

  private static final int SAMPLE_SIZE = 100;
  private static final String[] NODES = {"node0", "node1", "node2"};

  protected static Collection<UUID> gen(int n) {
    return IntStream.range(0, n)
        .mapToObj((ignored) -> UUID.randomUUID())
        .collect(Collectors.toList());
  }

  protected abstract HashRing<String> ring(
      final int pointsPerNode,
      final BiFunction<Integer, String, HashedNodePoint<String>> factory)
      throws Exception;

  @Test
  public void equalRingsMustAssignToTheSameNodes() throws Exception {
    HashRing<String> ring1 = includeAll(ring(POINTS_PER_NODE, FACTORY), NODES);
    HashRing<String> ring2 = includeAll(ring(POINTS_PER_NODE, FACTORY), NODES);

    for (UUID sample : gen(SAMPLE_SIZE)) {
      assertEquals("Inconsistent assignment between equal rings",
          ring1.nodeOf(sample), ring2.nodeOf(sample));
    }
  }

  private static HashRing<String> includeAll(
      final HashRing<String> ring, String... nodes) {
    for (String node : nodes) {
      ring.includeNode(node);
    }
    return ring;
  }

  @Test
  public void excludingNodesMustRetainAssignmentsToRemainingNodes()
      throws Exception {
    HashRing<String> ring = includeAll(ring(POINTS_PER_NODE, FACTORY), NODES);
    Map<String, Set<UUID>> assignments = assignments(ring);

    HashRing<String> removed = excludeAll(ring, NODES[NODES.length - 1]);
    Map<String, Set<UUID>> assignmentsRemoved = assignments(removed);

    System.out.println(assignmentsRemoved.get(NODES[0])
        .containsAll(assignments.get(NODES[0])));
    System.out.println(assignmentsRemoved.get(NODES[1])
        .containsAll(assignments.get(NODES[1])));
  }

  private static Map<String, Set<UUID>> assignments(HashRing<String> ring) {
    Map<String, List<Tuple2<UUID, String>>> map = gen(SAMPLE_SIZE).stream()
        .map((uuid) -> Tuple2.from(uuid, ring.nodeOf(uuid)))
        .collect(Collectors.groupingBy((tuple) -> tuple._2));
    Map<String, Set<UUID>> finalMap = new HashMap<>(map.size());
    map.forEach((key, value) ->
        finalMap.put(key, value.stream().map(t -> t._1)
            .collect(Collectors.toSet())));
    return finalMap;
  }

  private static HashRing<String> excludeAll(
      final HashRing<String> ring, String... nodes) {
    for (String node : nodes) {
      ring.excludeNode(node);
    }
    return ring;
  }
}