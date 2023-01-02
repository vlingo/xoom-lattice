// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.List;

import io.vlingo.xoom.common.Completes;

/**
 * Manages the multiple confirmations of {@code Projectable}s until the full
 * count are virtually confirmed, after which the actual confirmation is performed.
 */
public interface MultiConfirming {
  static final long DefaultExpirationLimit = 3000;

  /**
   * Include the {@code projectable} to manage its confirmations to {@code count} times
   * and then perform the actual confirmation.
   * @param projectable the Projectable to manage
   * @param count the int number of times that confirmation must occur for final confirmation
   */
  void manageConfirmationsFor(final Projectable projectable, final int count);

  /**
   * Answer a {@code List<Projectable>} of managed {@code Projectable}s within a {@code Completes<List<Projectable>>}.
   * @return {@code Completes<List<Projectable>>}
   */
  Completes<List<Projectable>> managedConfirmations();
}
