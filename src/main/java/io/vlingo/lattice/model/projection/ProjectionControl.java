// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

public interface ProjectionControl {
  void confirmProjected(final String projectionId);

  default Confirmer confirmerFor(final Projectable projectable) {
    return () -> this.confirmProjected(projectable.projectionId());
  }

  @FunctionalInterface
  public static interface Confirmer {
    void confirm();
  }
}
