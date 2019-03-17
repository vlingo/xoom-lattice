// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.lattice.exchange.ExchangeReceiver;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepFour;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepThree;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepTwo;
import io.vlingo.lattice.model.process.FiveStepProcess.MarkCompleted;

public class ExchangeReceivers {
  public final AccessSafely access;

  public final DoStepTwoReceiver doStepTwoReceiver;
  public final DoStepThreeReceiver doStepThreeReceiver;
  public final DoStepFourReceiver doStepFourReceiver;
  public final MarkCompletedReceiver markCompletedReceiver;

  private final AtomicInteger stepCount;
  private FiveStepProcess process;

  public ExchangeReceivers() {
    this.doStepTwoReceiver = new DoStepTwoReceiver();
    this.doStepThreeReceiver = new DoStepThreeReceiver();
    this.doStepFourReceiver = new DoStepFourReceiver();
    this.markCompletedReceiver = new MarkCompletedReceiver();

    this.stepCount = new AtomicInteger(1);

    this.access = AccessSafely.afterCompleting(4);
    this.access
      .writingWith("stepCount", (Integer delta) -> stepCount.set(stepCount.get() + delta))
      .readingWith("stepCount", () -> stepCount.get());
  }

  public void process(final FiveStepProcess process) {
    this.process = process;
  }

  public final class DoStepTwoReceiver implements ExchangeReceiver<DoStepTwo> {
    @Override
    public void receive(final DoStepTwo message) {
      process.stepTwoHappened();
      access.writeUsing("stepCount", 1);
    }
  }

  public final class DoStepThreeReceiver implements ExchangeReceiver<DoStepThree> {
    @Override
    public void receive(final DoStepThree message) {
      process.stepThreeHappened();
      access.writeUsing("stepCount", 1);
    }
  }

  public final class DoStepFourReceiver implements ExchangeReceiver<DoStepFour> {
    @Override
    public void receive(final DoStepFour message) {
      process.stepFourHappened();
      access.writeUsing("stepCount", 1);
    }
  }

  public final class MarkCompletedReceiver implements ExchangeReceiver<MarkCompleted> {
    @Override
    public void receive(final MarkCompleted message) {
      process.stepFiveHappened();
      access.writeUsing("stepCount", 1);
    }
  }
}
