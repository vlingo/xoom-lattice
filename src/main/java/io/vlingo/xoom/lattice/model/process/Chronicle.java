// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

/**
 * State machine state management.
 * @param <S> the type of state this I maintain
 */
public class Chronicle<S> {
  public final S state;

  public Chronicle(final S state) {
    this.state = state;
  }

  public Chronicle<S> transitionTo(final S state) {
    return new Chronicle<>(state);
  }
}
