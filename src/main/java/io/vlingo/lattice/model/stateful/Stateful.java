// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import java.util.function.BiConsumer;

public interface Stateful<S> {
  String id();
  void preserve(final S state, final String metadata, final String operation, final BiConsumer<S,Integer> consumer);
  void restore(final BiConsumer<S,Integer> consumer);
  void state(final S state, final int stateVersion);
  Class<?> stateType();
  int stateVersion();
  int typeVersion();

  default void preserve(final S state, final String operation, final BiConsumer<S,Integer> consumer) {
    preserve(state, "", operation, consumer);
  }

  default void preserve(final S state, final BiConsumer<S,Integer> consumer) {
    preserve(state, "", "", consumer);
  }

  default void preserve(final S state, final String operation) {
    preserve(state, "", operation, null);
  }

  default void preserve(final S state) {
    preserve(state, null, null);
  }

  default void restore() {
    restore(null);
  }
}
