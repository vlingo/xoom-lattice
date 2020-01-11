// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.hashring;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.BiFunction;

public abstract class MD5HashRing<T> implements HashRing<T> {
  protected final BiFunction<Integer, T, HashedNodePoint<T>> factory;
  protected final MessageDigest hasher;
  protected final int pointsPerNode;

  protected MD5HashRing(final int pointsPerNode, final BiFunction<Integer, T, HashedNodePoint<T>> factory) throws Exception {
    this.pointsPerNode = pointsPerNode;
    this.factory = factory;
    this.hasher = MessageDigest.getInstance("MD5");
  }

  protected int hashed(final Object id) {
    hasher.reset();
    hasher.update(StandardCharsets.UTF_8.encode(id.toString()));
    final int hash = Arrays.hashCode(hasher.digest());
    return hash;
  }

  protected HashedNodePoint<T> hashedNodePointOf(final Object id) {
    final HashedNodePoint<T> hashedNodePointOf = factory.apply(hashed(id), null);
    return hashedNodePointOf;
  }
}
