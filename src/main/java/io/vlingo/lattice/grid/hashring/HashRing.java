// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.hashring;

public interface HashRing<T> {
  void dump();
  HashRing<T> excludeNode(final T nodeIdentifier);
  HashRing<T> includeNode(final T nodeIdentifier);
  T nodeOf(final Object id);
}
