// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.hashring;

import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.TreeMap;

import io.vlingo.xoom.common.pool.ElasticResourcePool;
import io.vlingo.xoom.common.pool.ResourceFactory;
import io.vlingo.xoom.common.pool.ResourcePool;

public class MurmurSortedMapHashRing<T> implements HashRing<T> {

  private static final int DefaultSeed = 31;

  private final int pointsPerNode;
  private final int seed;

  private final ResourcePool<ByteBuffer, Void> byteBufferPool =
      new ElasticResourcePool<>(ElasticResourcePool.Config.of(10),
          new ResourceFactory<ByteBuffer, Void>() {
            @Override
            public Class<ByteBuffer> type() {
              return ByteBuffer.class;
            }

            @Override
            public ByteBuffer create(Void aVoid) {
              return ByteBuffer.allocate(36);
            }

            @Override
            public Void defaultArguments() {
              return null;
            }

            @Override
            public ByteBuffer reset(ByteBuffer byteBuffer, Void aVoid) {
              byteBuffer.clear();
              return byteBuffer;
            }

            @Override
            public void destroy(ByteBuffer byteBuffer) {
            }
          });

  private final SortedMap<Integer, T> ring;

  public MurmurSortedMapHashRing(final int pointsPerNode) {
    this(pointsPerNode, DefaultSeed);
  }

  public MurmurSortedMapHashRing(final int pointsPerNode, final int seed) {
    this.pointsPerNode = pointsPerNode;
    this.seed = seed;
    this.ring = new TreeMap<>();
  }

  private MurmurSortedMapHashRing(final int pointsPerNode, final int seed, final SortedMap<Integer, T> ring) {
    this.pointsPerNode = pointsPerNode;
    this.seed = seed;
    this.ring = ring;
  }


  @Override
  public void dump() {
    System.out.println("NODES: " + ring.size());
    for (final T hashedNodePoint : ring.values()) {
      System.out.println("NODE: " + hashedNodePoint);
    }
  }

  @Override
  public HashRing<T> includeNode(T nodeIdentifier) {
    for (int i = 0; i < pointsPerNode; i++) {
      final int hash = hashed(nodeIdentifier.toString() + i);
      ring.put(hash, nodeIdentifier);
    }
    return this;
  }

  private int hashed(final Object id) {
    final ByteBuffer buffer = byteBufferPool.acquire();
    final int hash;
    try {
      buffer.put(id.toString().getBytes());
      hash = MurmurHash.hash32(buffer, 0, buffer.position(), seed);
    }
    finally {
      byteBufferPool.release(buffer);
    }
    return hash;
  }

  @Override
  public HashRing<T> excludeNode(T nodeIdentifier) {
    for (int i = 0; i < pointsPerNode; i++) {
      final int hash = hashed(nodeIdentifier.toString() + i);
      ring.remove(hash);
    }
    return this;
  }

  @Override
  public T nodeOf(Object id) {
    if (ring.isEmpty()) {
      return null;
    }
    int hash = hashed(id);
    if (!ring.containsKey(hash)) {
      SortedMap<Integer, T> tailMap =
          ring.tailMap(hash);
      hash = tailMap.isEmpty() ?
          ring.firstKey() : tailMap.firstKey();
    }
    return ring.get(hash);
  }

  @Override
  @SuppressWarnings("unchecked")
  public HashRing<T> copy() {
    final TreeMap<Integer, T> _ring = (TreeMap<Integer, T>) this.ring;
    return new MurmurSortedMapHashRing<>(this.pointsPerNode, this.seed, (TreeMap<Integer, T>) _ring.clone());
  }
}
