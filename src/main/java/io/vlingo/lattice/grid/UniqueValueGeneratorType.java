// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.lattice.grid.UniqueValueGenerator.NameBasedUniqueValueGenerator;
import io.vlingo.lattice.grid.UniqueValueGenerator.RandomUniqueValueGenerator;
import io.vlingo.lattice.grid.UniqueValueGenerator.TimeBasedUniqueValueGenerator;

public enum UniqueValueGeneratorType {
  TimeBased { public UniqueValueGenerator generator() { return new TimeBasedUniqueValueGenerator(); } },
  NameBased { public UniqueValueGenerator generator() { return new NameBasedUniqueValueGenerator(); } },
  Random    { public UniqueValueGenerator generator() { return new RandomUniqueValueGenerator(); } };
  
  public UniqueValueGenerator generator() { return null; }
}
