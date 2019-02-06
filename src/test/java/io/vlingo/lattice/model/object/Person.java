// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import io.vlingo.common.Completes;

public interface Person {
  Completes<PersonState> current();
  Completes<PersonState> identify(final String name, final int age);
  Completes<PersonState> change(final String name);
  Completes<PersonState> increaseAge();
}
