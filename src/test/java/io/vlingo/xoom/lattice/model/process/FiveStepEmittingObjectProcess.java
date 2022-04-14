// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.xoom.common.Completes;

public class FiveStepEmittingObjectProcess extends ObjectProcess<StepCountObjectState> implements FiveStepProcess {
  private static final AtomicLong IdGenerator = new AtomicLong(0);

  private final Chronicle<StepCountObjectState> chronicle;
  private StepCountObjectState state;

  public FiveStepEmittingObjectProcess() {
    this(IdGenerator.incrementAndGet());
  }

  public FiveStepEmittingObjectProcess(final long id) {
    super(String.valueOf(id));
    this.state = new StepCountObjectState(id);
    this.chronicle = new Chronicle<>(state);
  }

  @Override
  public Chronicle<StepCountObjectState> chronicle() {
    return chronicle;
  }

  @Override
  public Completes<Integer> queryStepCount() {
    return completes().with(state.stepCount());
  }

  @Override
  public void stepOneHappened() {
    state.countStep();
    process(new DoStepTwo());
  }

  @Override
  public void stepTwoHappened() {
    state.countStep();
    process(new DoStepThree());
  }

  @Override
  public void stepThreeHappened() {
    state.countStep();
    process(new DoStepFour());
  }

  @Override
  public void stepFourHappened() {
    state.countStep();
    process(new DoStepFive());
  }

  @Override
  public void stepFiveHappened() {
    state.countStep();
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  protected StepCountObjectState stateObject() {
    return state;
  }

  @Override
  protected void stateObject(final StepCountObjectState state) {
    this.state = state;
    this.chronicle.transitionTo(state);
  }

  @Override
  protected Class<StepCountObjectState> stateObjectType() {
    return StepCountObjectState.class;
  }
}
