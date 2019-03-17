// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.common.Completes;

public class FiveStepSourcedProcess extends SourcedProcess implements FiveStepProcess {
  private int stepCount;

  public FiveStepSourcedProcess() { }

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
    send(new MarkCompleted());
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
