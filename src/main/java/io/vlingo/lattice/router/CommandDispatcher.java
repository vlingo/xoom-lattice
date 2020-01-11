// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.lattice.model.Command;

@FunctionalInterface
public interface CommandDispatcher<P,C extends Command,A> {
  void accept(final P protocol, final C command, final A answer);
}
