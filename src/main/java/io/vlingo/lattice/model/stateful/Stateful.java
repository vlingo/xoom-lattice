// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import java.util.function.Supplier;

public interface Stateful<S> {
  String id();
  <RT> void preserve(final S state, final String metadata, final String operation, final Supplier<RT> andThen);
  void restore();
  void state(final S state);
  Class<S> stateType();

  default <RT> void preserve(final S state, final String operation, final Supplier<RT> andThen) {
    preserve(state, "", operation, andThen);
  }

  default <RT> void preserve(final S state, final Supplier<RT> andThen) {
    preserve(state, "", "", andThen);
  }

  default void preserve(final S state, final String metadata, final String operation) {
    preserve(state, "", operation, null);
  }

  default void preserve(final S state, final String operation) {
    preserve(state, "", operation, null);
  }

  default void preserve(final S state) {
    preserve(state, null, null, null);
  }
}
