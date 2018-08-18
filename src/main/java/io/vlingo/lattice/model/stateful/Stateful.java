// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

public interface Stateful<T> {
  String id();
  void preserve(final T state);
  void restore();
  T state();
  void state(final T state, final int stateVersion);
  Class<?> stateType();
  int stateVersion();
  int typeVersion();
}
