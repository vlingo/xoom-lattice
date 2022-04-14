// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.io.Serializable;
import java.util.UUID;

import io.vlingo.xoom.actors.UUIDAddress;

public final class GridAddress extends UUIDAddress implements Serializable {
  private static final long serialVersionUID = -7172480689137574451L;

  @Override
  public boolean isDistributable() {
    return true;
  }

  GridAddress(final UUID reservedId) {
    this(reservedId, null, false);
  }

  GridAddress(final UUID reservedId, final String name) {
    this(reservedId, name, false);
  }

  GridAddress(final UUID reservedId, final String name, final boolean prefixName) {
    super(reservedId, name, prefixName);
  }
}
