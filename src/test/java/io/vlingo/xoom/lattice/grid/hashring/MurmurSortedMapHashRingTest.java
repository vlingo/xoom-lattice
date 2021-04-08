package io.vlingo.xoom.lattice.grid.hashring;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

}
