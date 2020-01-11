// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model;

import io.vlingo.symbio.Source;

/**
 * A abstract base for events, which are considered a type of {@code Source}.
 */
public abstract class DomainEvent extends Source<DomainEvent> {
  /**
   * Construct my default state with a type version of 1.
   */
  protected DomainEvent() {
    super();
  }

  /**
   * Construct my default state with a {@code eventTypeVersion} greater than 1.
   * @param eventTypeVersion the int version of this event type
   */
  protected DomainEvent(final int eventTypeVersion) {
    super(eventTypeVersion);
  }
}
