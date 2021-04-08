// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model;

import io.vlingo.xoom.symbio.Source;

public class TestEvents {

  public static abstract class Event extends Source<Event> {
    @Override
    public boolean equals(final Object other) {
      if (other == null || other.getClass() != getClass()) {
        return false;
      }
      return true;
    }
  }

  public static final class Event1 extends Event { }

  public static final class Event2 extends Event { }

  public static final class Event3 extends Event { }
}
