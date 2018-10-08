// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.function.BiConsumer;

public class TestCommandSourcedEntity extends CommandSourced implements Entity {
  static {
    final BiConsumer<TestCommandSourcedEntity,DoCommand1> bi1 = TestCommandSourcedEntity::applied1;
    registerConsumer(TestCommandSourcedEntity.class, DoCommand1.class, bi1);
    final BiConsumer<TestCommandSourcedEntity,DoCommand2> bi2 = TestCommandSourcedEntity::applied2;
    registerConsumer(TestCommandSourcedEntity.class, DoCommand2.class, bi2);
  }

  private final Result result;

  public TestCommandSourcedEntity(final Result result) {
    this.result = result;
    apply(new DoCommand1());
  }

  public void doTest2() {
    apply(new DoCommand2());
  }

  private void applied1(final DoCommand1 command) {
    result.tested1 = true;
    result.applied.add(command);
    result.until.happened();
  }

  private void applied2(final DoCommand2 command) {
    result.tested2 = true;
    result.applied.add(command);
    result.until.happened();
  }
}
