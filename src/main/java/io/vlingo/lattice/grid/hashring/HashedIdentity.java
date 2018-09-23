// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.hashring;

public abstract class HashedIdentity implements Comparable<Integer> {
  private int hash;

  public HashedIdentity(final int hash) {
    this.hash = hash;
  }

  public int hash() {
    return hash;
  }

  @Override
  public int compareTo(final Integer otherHash) {
    return otherHash - hash;
  }

  @Override
  public String toString() {
    return "HashedIdentity[hash=" + hash() + "]";
  }
}
