// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.actors.testkit.TestUntil;

public class Result {
  public List<Object> applied = new ArrayList<>();
  public boolean tested1;
  public boolean tested2;
  public boolean tested3;
  public TestUntil until = TestUntil.happenings(0);
}
