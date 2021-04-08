// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.hashring;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;

public class MD5ArrayHashRing<T> extends MD5HashRing<T> implements HashRing<T> {
  private HashedNodePoint<T>[] hashedNodePoints;

  @SuppressWarnings("unchecked")
  public MD5ArrayHashRing(final int pointsPerNode, final BiFunction<Integer, T, HashedNodePoint<T>> factory) throws Exception {
    super(pointsPerNode, factory);

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
      hashedNodePoints[element] = factory.apply(hash, nodeIdentifier);
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
