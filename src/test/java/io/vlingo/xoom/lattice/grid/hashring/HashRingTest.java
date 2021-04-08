// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.hashring;

import org.junit.Ignore;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * And the consistent winner is MurmurArrayHashRing.
 */
@Ignore
public class HashRingTest {
  private static final int ELEMENTS = 1_000_000;
  private static final int NODES = 5;
  private static final int POINTS_PER_NODE = 100;

  private int excluded = 0;
  private int included = 0;

  private final BiFunction<Integer, String, HashedNodePoint<String>> factory = (hash, node) -> {
    return new HashedNodePoint<String>(hash, node) {
      @Override
      public void excluded() {
        ++excluded;
      }

      @Override
      public void included() {
        ++included;
      }
    };
  };

  @Test
  public void testMD5ListHashRing() throws Exception {
    System.out.println("\ntestMD5ListHashRing()\n============================");

    final HashRing<String> ring = new MD5ArrayHashRing<>(POINTS_PER_NODE, factory);
    includeNodes(ring);
    final int elementsPerNode[] = new int[NODES];

    populateElements(ring, elementsPerNode);

    dump(elementsPerNode);
  }

  @Test
  public void testMD5ArrayHashRing() throws Exception {
    System.out.println("\ntestMD5ArrayHashRing()\n============================");

    final HashRing<String> ring = new MD5ListHashRing<>(POINTS_PER_NODE, factory);
    includeNodes(ring);
    final int elementsPerNode[] = new int[NODES];

    populateElements(ring, elementsPerNode);

    dump(elementsPerNode);
  }

  @Test
  public void testMurmurArrayHashRing() {
    // The consistent winner: MurmurArrayHashRing

    System.out.println("\ntestMurmurArrayHashRing()\n============================");


    final HashRing<String> ring = new MurmurArrayHashRing<>(POINTS_PER_NODE, factory);

    includeNodes(ring);
    final int elementsPerNode[] = new int[NODES];

    populateElements(ring, elementsPerNode);

    dump(elementsPerNode);

    assertEquals(0, excluded);
    assertEquals(NODES * POINTS_PER_NODE, included);
  }

  private void dump(final int elementsPerNode[]) {
    for (int idx = 0; idx < NODES; ++idx) {
      System.out.println("node" + idx + "=" + elementsPerNode[idx]);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void includeNodes(final HashRing<T> ring) {
    for (int idx = 0; idx < NODES; ++idx) {
      ring.includeNode((T) ("node" + idx));
    }
  }

  private <T> void populateElements(final HashRing<T> ring, final int elementsPerNode[]) {
    final long startTime = System.currentTimeMillis();

    for (int idx = 0; idx < ELEMENTS; ++idx) {
      final T node = ring.nodeOf(UUID.randomUUID().toString());
      final int nodeIndex = node.toString().charAt(4) - '0';
      ++elementsPerNode[nodeIndex];
    }

    System.out.println("Time in ms: " + (System.currentTimeMillis() - startTime));
  }
}
