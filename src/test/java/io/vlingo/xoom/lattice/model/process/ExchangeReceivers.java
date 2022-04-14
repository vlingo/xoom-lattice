// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.lattice.exchange.ExchangeReceiver;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepFive;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepFour;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepOne;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepThree;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepTwo;

public class ExchangeReceivers {
  public final AccessSafely access;

  public final DoStepOneReceiver doStepOneReceiver;
  public final DoStepTwoReceiver doStepTwoReceiver;
  public final DoStepThreeReceiver doStepThreeReceiver;
  public final DoStepFourReceiver doStepFourReceiver;
  public final DoStepFiveReceiver doStepFiveReceiver;

  private final AtomicInteger stepCount;
  private FiveStepProcess process;

  public ExchangeReceivers() {
    this.doStepOneReceiver = new DoStepOneReceiver();
    this.doStepTwoReceiver = new DoStepTwoReceiver();
    this.doStepThreeReceiver = new DoStepThreeReceiver();
    this.doStepFourReceiver = new DoStepFourReceiver();
    this.doStepFiveReceiver = new DoStepFiveReceiver();

    this.stepCount = new AtomicInteger(0);

    this.access = AccessSafely.afterCompleting(5);
    this.access
      .writingWith("stepCount", (Integer delta) -> stepCount.incrementAndGet())
      .readingWith("stepCount", () -> stepCount.get());
  }

  public void process(final FiveStepProcess process) {
    this.process = process;
  }

  public final class DoStepOneReceiver implements ExchangeReceiver<DoStepOne> {
    @Override
    public void receive(final DoStepOne message) {
      process.stepOneHappened();
      access.writeUsing("stepCount", 1);
    }
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

  public final class DoStepFiveReceiver implements ExchangeReceiver<DoStepFive> {
    @Override
    public void receive(final DoStepFive message) {
      process.stepFiveHappened();
      access.writeUsing("stepCount", 1);
    }
  }
}
