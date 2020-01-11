// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.common.Completes;

public class FiveStepSendingSourcedProcess extends SourcedProcess<Object> implements FiveStepProcess {
  private int stepCount;

  public FiveStepSendingSourcedProcess() { }

  @Override
  public Completes<Integer> queryStepCount() {
    return completes().with(stepCount);
  }

  @Override
  public void stepOneHappened() {
    ++stepCount;
    send(new DoStepTwo());
  }

  @Override
  public void stepTwoHappened() {
    ++stepCount;
    send(new DoStepThree());
  }

  @Override
  public void stepThreeHappened() {
    ++stepCount;
    send(new DoStepFour());
  }

  @Override
  public void stepFourHappened() {
    ++stepCount;
    send(new DoStepFive());
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
}
