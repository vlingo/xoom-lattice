// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.function.BiConsumer;

import io.vlingo.common.Completes;

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
    this.result = result;
    apply(new Test1Happened());
  }

  @Override
  public void doTest2() {
    apply(new Test2Happened());
  }

  @Override
  public Completes<String> doTest3() {
    apply(new Test3Happened(), () -> "hello");
    return completes();
  }

  @Override
  protected String streamName() {
    return "TestEvent123";
  }

  private void applied1(final Test1Happened event) {
    result.tested1 = true;
    result.applied.add(event);
    result.until.happened();
  }

  private void applied2(final Test2Happened event) {
    result.tested2 = true;
    result.applied.add(event);
    result.until.happened();
  }

  private void applied3(final Test3Happened event) {
    result.tested3 = true;
    result.applied.add(event);
    result.until.happened();
  }
}
