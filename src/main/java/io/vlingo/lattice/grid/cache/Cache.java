// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.cache;

public class Cache {
  static final String DefaultCacheName = "__defaultCache";

  private final String name;

  public Cache(final String name) {
    this.name = name;
  }

  public Cache() {
    this.name = DefaultCacheName;
  }

  @Override
  public String toString() {
    return "Cache[name=" + name + "]";
  }
}
