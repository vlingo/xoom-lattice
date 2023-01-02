// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

/**
 * Defines the basic protocol for all {@code Projection} types.
 */
public interface Projection {

  /**
   * Project the given {@code Projectable} that is managed by the given {@code ProjectionControl}.
   * @param projectable the Projectable to project
   * @param control the ProjectionControl that manages the results of the Projectable
   */
  void projectWith(final Projectable projectable, final ProjectionControl control);
}
