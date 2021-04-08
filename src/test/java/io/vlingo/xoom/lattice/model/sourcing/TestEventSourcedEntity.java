// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.sourcing;

import java.util.function.BiConsumer;

import io.vlingo.xoom.common.Completes;

public class TestEventSourcedEntity extends EventSourced implements Entity {
  static {
    final BiConsumer<TestEventSourcedEntity,Test1Happened> bi1 = TestEventSourcedEntity::applied1;
    registerConsumer(TestEventSourcedEntity.class, Test1Happened.class, bi1);
    final BiConsumer<TestEventSourcedEntity,Test2Happened> bi2 = TestEventSourcedEntity::applied2;
    registerConsumer(TestEventSourcedEntity.class, Test2Happened.class, bi2);
    final BiConsumer<TestEventSourcedEntity,Test3Happened> bi3 = TestEventSourcedEntity::applied3;
    registerConsumer(TestEventSourcedEntity.class, Test3Happened.class, bi3);
  }

  private final Result result;

  public TestEventSourcedEntity(final Result result) {
    super("TestEvent123");
    this.result = result;
  }

  @Override
  public void doTest1() {
    apply(new Test1Happened());
  }

  @Override
  public void doTest2() {
    apply(new Test2Happened());
  }

  @Override
  public Completes<String> doTest3() {
    return apply(new Test3Happened(), () -> "hello");
  }

  private void applied1(final Test1Happened event) {
    result.access().writeUsing("tested1", true);
    result.access().writeUsing("applied", event);
  }

  private void applied2(final Test2Happened event) {
    result.access().writeUsing("tested2", true);
    result.access().writeUsing("applied", event);
  }

  private void applied3(final Test3Happened event) {
    result.access().writeUsing("tested3", true);
    result.access().writeUsing("applied", event);
  }
}
