// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model;

/**
 * Provides the means to request the identity of the {@code DomainEvent}.
 */
public abstract class IdentifiedDomainEvent extends DomainEvent {
  /**
   * Answer the {@code String} identity of this {@code DomainEvent}.
   * @return String
   */
  public abstract String identity();
}
