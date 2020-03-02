// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.common.Completes;

public class FiveStepEmittingObjectProcess extends ObjectProcess<StepCountObjectState> implements FiveStepProcess {
  private final Chronicle<StepCountObjectState> chronicle;
  private StepCountObjectState state;

  public FiveStepEmittingObjectProcess() {
    this.state = new StepCountObjectState();
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
    return String.valueOf(state.persistenceId());
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

  @Override
  public void applyRelocationSnapshot(String snapshot) {
    stateObject(new StepCountObjectState(snapshot));
  }
}
