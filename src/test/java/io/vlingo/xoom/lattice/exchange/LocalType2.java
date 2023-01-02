// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

public class LocalType2 {
  public final String attribute1;
  public final int attribute2;

  public LocalType2(final String value1, final int value2) {
    this.attribute1 = value1;
    this.attribute2 = value2;
  }

  @Override
  public int hashCode() {
    return 31 * attribute1.hashCode() * Integer.hashCode(attribute2);
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != getClass()) {
      return false;
    }

    final LocalType2 otherLocalType = (LocalType2) other;

    return this.attribute1.equals(otherLocalType.attribute1) && attribute2 == otherLocalType.attribute2;
  }

  @Override
  public String toString() {
    return "LocalType2[attribute1=" + attribute1 + " attribute2=" + attribute2 + "]";
  }
}
