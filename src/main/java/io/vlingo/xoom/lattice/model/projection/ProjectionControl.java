// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

/**
 * Defines the control management for a given {@code Projection}.
 */
public interface ProjectionControl {
  /**
   * Answer the {@code Confirmer} for the given {@code Projectable} through
   * which confirmation can be performed as a single operation.
   * @param projectable the Projectable requiring confirmation of completed projections operations
   * @param control the ProjectionControl
   * @return Confirmer
   */
  static Confirmer confirmerFor(final Projectable projectable, final ProjectionControl control) {
    return () -> control.confirmProjected(projectable.projectionId());
  }

  /**
   * Confirms that all projection operations have been completed.
   * @param projectionId the String unique identity of the projection operation
   */
  void confirmProjected(final String projectionId);

  /**
   * Defines the functional interface used to confirm the completion of projections operations.
   */
  @FunctionalInterface
  public static interface Confirmer {
    /**
     * Confirms the completion of projections operations.
     */
    void confirm();
  }
}
