// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.hashring;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;

public class MurmurArrayHashRing<T> implements HashRing<T> {
  private static final int DefaultSeed = 31;

  private final ByteBuffer buffer;
  private final BiFunction<Integer, T, HashedNodePoint<T>> factory;
  private HashedNodePoint<T>[] hashedNodePoints;
  private final int pointsPerNode;
  private final int seed;

  public MurmurArrayHashRing(final int pointsPerNode, final BiFunction<Integer, T, HashedNodePoint<T>> factory) {
    this(pointsPerNode, factory, DefaultSeed);
  }

  @SuppressWarnings("unchecked")
  public MurmurArrayHashRing(final int pointsPerNode, final BiFunction<Integer, T, HashedNodePoint<T>> factory, final int seed) {
    this.pointsPerNode = pointsPerNode;
    this.factory = factory;
    this.seed = seed;
    this.buffer = ByteBuffer.allocate(64);
    this.hashedNodePoints = new HashedNodePoint[0];
  }

  @Override
  public void dump() {
    System.out.println("NODES: " + hashedNodePoints.length);
    for (final HashedNodePoint<T> hashedNodePoint : hashedNodePoints) {
      System.out.println("NODE: " + hashedNodePoint);
    }
  }

  @Override
  public HashRing<T> excludeNode(final T nodeIdentifier) {
    final HashedNodePoint<T>[] exclusive = less();
    int index = 0;
    int element = 0;
    int hash = hashed(nodeIdentifier.toString() + element);
    for (final HashedNodePoint<T> hashedNodePoint : hashedNodePoints) {
      if (hashedNodePoint.hash() == hash) {
        hashedNodePoint.excluded();
        hash = hashed(nodeIdentifier.toString() + (++element));
      } else {
        exclusive[index++] = hashedNodePoint;
      }
    }

    hashedNodePoints = exclusive;

    return this;
  }

  @Override
  public HashRing<T> includeNode(final T nodeIdentifier) {
    final int startingAt = moreStartingAt();
    for (int element = startingAt; element < hashedNodePoints.length; ++element) {
      final int hash = hashed(nodeIdentifier.toString() + element);
      final HashedNodePoint<T> hashedNodePoint = factory.apply(hash, nodeIdentifier);
      hashedNodePoints[element] = hashedNodePoint;
      hashedNodePoint.included();
    }
    Arrays.sort(hashedNodePoints, Comparator.comparingInt(HashedIdentity::hash));

    return this;
  }

  @Override
  public T nodeOf(final Object id) {
    final HashedNodePoint<T> hashedNodePoint = hashedNodePointOf(id);
    int index = Arrays.binarySearch(hashedNodePoints, hashedNodePoint, Comparator.comparingInt(HashedIdentity::hash));
    if (index < 0) {
      index = -index;
      if (index >= hashedNodePoints.length) {
        index = 0;
      }
    }
    return hashedNodePoints[index].nodeIdentifier;
  }

  private int hashed(final Object id) {
    buffer.clear();
    buffer.put(id.toString().getBytes());
    return MurmurHash.hash32(buffer, 0, buffer.position(), seed);
  }

  private HashedNodePoint<T> hashedNodePointOf(final Object id) {
    final HashedNodePoint<T> hashedNodePoint = factory.apply(hashed(id), null);
    return hashedNodePoint;
  }

  @SuppressWarnings("unchecked")
  private HashedNodePoint<T>[] less() {
    final HashedNodePoint<T>[] less = new HashedNodePoint[hashedNodePoints.length - pointsPerNode];
    return less;
  }

  @SuppressWarnings("unchecked")
  private int moreStartingAt() {
    final HashedNodePoint<T>[] previous = hashedNodePoints;
    hashedNodePoints = new HashedNodePoint[previous.length + pointsPerNode];
    System.arraycopy(previous, 0, hashedNodePoints, 0, previous.length);
    return previous.length;
  }
}
