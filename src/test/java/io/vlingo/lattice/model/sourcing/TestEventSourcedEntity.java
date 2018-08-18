// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.function.BiConsumer;

public class TestEventSourcedEntity extends EventSourced {
  static {
    final BiConsumer<TestEventSourcedEntity,Test1Happened> bi1 = TestEventSourcedEntity::applied1;
    registerConsumer(TestEventSourcedEntity.class, Test1Happened.class, bi1);
    final BiConsumer<TestEventSourcedEntity,Test2Happened> bi2 = TestEventSourcedEntity::applied2;
    registerConsumer(TestEventSourcedEntity.class, Test2Happened.class, bi2);
  }

  public boolean tested1;
  public boolean tested2;

  public TestEventSourcedEntity() {
    apply(new Test1Happened());
  }

  public void doTest2() {
    apply(new Test2Happened());
  }

  public void applied1(final Test1Happened event) {
    tested1 = true;
  }

  public void applied2(final Test2Happened event) {
    tested2 = true;
  }
}
