package io.vlingo.lattice.grid.hashring;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class MurmurSortedMapHashRingTest {

  @Test
  public void testNodeOfRace() {
    MurmurSortedMapHashRing<String> ring = new MurmurSortedMapHashRing<>(100);

    ring.includeNode("node1");
    ring.includeNode("node2");
    ring.includeNode("node3");

    final ExecutorService exec = Executors.newFixedThreadPool(32);
    CompletionService<String> completionService =
        new ExecutorCompletionService<>(exec);

    Callable<String> call = () -> ring.nodeOf("testing");

    List<Future<String>> futures = IntStream.range(0, 1000)
        .mapToObj((int i) -> completionService.submit(call))
        .collect(Collectors.toList());

    Set<String> results = new HashSet<>();
    for (@SuppressWarnings("unused") Future<String> f : futures) {
      try {
        results.add(completionService.take().get());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    assertEquals(1, results.size());
  }

  @Test
  public void testNodesOf() {
    MurmurSortedMapHashRing<String> ring =
        new MurmurSortedMapHashRing<>(100);

    String of = "testing";

    List<String> nodes = new ArrayList<>(3);
    Collections.addAll(nodes, "node2", "node1", "node3");

    nodes.forEach(ring::includeNode);

    List<String> nodesOf = ring.nodesOf(of);

    assertEquals(3, nodesOf.size());
    assertEquals(nodes, nodesOf);
  }

}
