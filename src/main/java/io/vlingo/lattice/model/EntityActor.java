// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model;

import java.io.Serializable;

import io.vlingo.actors.Actor;

/**
 * Abstract base of all entity types.
 */
public abstract class EntityActor extends Actor implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Restore my state.
   */
  protected abstract void restore();
}
