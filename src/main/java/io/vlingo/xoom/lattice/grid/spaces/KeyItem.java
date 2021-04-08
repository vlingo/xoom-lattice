// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

public final class KeyItem<T> extends Item<T> {
  public final Key key;

  public static <T> KeyItem<T> of(final Key key, final T object, final Lease lease) {
    return new KeyItem<>(key, object, lease);
  }

  private KeyItem(final Key key, final T object, final Lease lease) {
    super(object, lease);

    this.key = key;
  }
}
