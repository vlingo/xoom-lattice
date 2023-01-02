// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.hashring;

public abstract class HashedNodePoint<T> extends HashedIdentity {
  public final T nodeIdentifier;

  public HashedNodePoint(final int hash, final T nodeIdentifier) {
    super(hash);

    this.nodeIdentifier = nodeIdentifier;
  }

  public abstract void excluded();

  public abstract void included();

  @Override
  public String toString() {
    return "HashedNodePoint[hash=" + hash() + " nodeIdentifier=" + nodeIdentifier + "]";
  }
}
