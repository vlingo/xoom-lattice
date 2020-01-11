// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.common.Completes;

public class FiveStepEmittingSourcedProcess extends SourcedProcess<Object> implements FiveStepProcess {
  static {
    registerConsumer(FiveStepEmittingSourcedProcess.class, ProcessMessage.class, FiveStepEmittingSourcedProcess::applyProcessMessage);
  }

  private int stepCount;

  public FiveStepEmittingSourcedProcess() { }

  @Override
  public Completes<Integer> queryStepCount() {
    return completes().with(stepCount);
  }

  @Override
  public void stepOneHappened() {
    process(new DoStepTwo());
  }

  @Override
  public void stepTwoHappened() {
    process(new DoStepThree());
  }

  @Override
  public void stepThreeHappened() {
    process(new DoStepFour());
  }

  @Override
  public void stepFourHappened() {
    process(new DoStepFive());
  }

  @Override
  public void stepFiveHappened() {
    ++stepCount;
  }

  @Override
  public String id() {
    return "12345";
  }

  @Override
  protected String streamName() {
    return id();
  }

  private void applyProcessMessage(final ProcessMessage message) {
    ++stepCount;
  }
}
