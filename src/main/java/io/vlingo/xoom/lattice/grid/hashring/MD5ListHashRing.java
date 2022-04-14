// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.hashring;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

public class MD5ListHashRing<T> extends MD5HashRing<T> {
  private final List<HashedNodePoint<T>> hashedNodePoints;

  public MD5ListHashRing(final int pointsPerNode, final BiFunction<Integer, T, HashedNodePoint<T>> factory) throws Exception {
    super(pointsPerNode, factory);

    this.hashedNodePoints = new ArrayList<>();
  }

  @Override
  public void dump() {
    System.out.println("NODES: " + hashedNodePoints.size());
    for (final HashedNodePoint<T> hashedNodePoint : hashedNodePoints) {
      System.out.println("NODE: " + hashedNodePoint);
    }
  }

  @Override
  public HashRing<T> excludeNode(final T nodeIdentifier) {
    final ListIterator<HashedNodePoint<T>> iterator = hashedNodePoints.listIterator();
    int element = 0;
    int hash = hashed(nodeIdentifier.toString() + element);
    while (iterator.hasNext()) {
      final HashedNodePoint<T> hashedNodePoint = iterator.next();
      if (hashedNodePoint.hash() == hash) {
        hash = hashed(nodeIdentifier.toString() + (++element));
      } else {
        iterator.remove();
        hashedNodePoint.excluded();
      }
    }
    return this;
  }

  @Override
  public HashRing<T> includeNode(final T nodeIdentifier) {
    for (int element = 0; element < pointsPerNode; ++element) {
      hasher.reset();
      hasher.update(StandardCharsets.UTF_8.encode(nodeIdentifier.toString() + element));
      final int hash = Arrays.hashCode(hasher.digest());
      final HashedNodePoint<T> hashedNodePoint = factory.apply(hash, nodeIdentifier);
      hashedNodePoints.add(hashedNodePoint);
      hashedNodePoint.included();
    }
    Collections.sort(hashedNodePoints, Comparator.comparingInt(HashedIdentity::hash));

    return this;
  }

  @Override
  public T nodeOf(final Object id) {
    final HashedNodePoint<T> hashedNodePoint = hashedNodePointOf(id);
    int index = Collections.binarySearch(hashedNodePoints, hashedNodePoint, Comparator.comparingInt(HashedIdentity::hash));
    if (index < 0) {
      index = -index;
      if (index >= hashedNodePoints.size()) {
        index = 0;
      }
    }
    return hashedNodePoints.get(index).nodeIdentifier;
  }
}
