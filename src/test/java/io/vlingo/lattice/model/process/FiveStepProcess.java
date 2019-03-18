// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.common.Completes;
import io.vlingo.lattice.model.Command;

public interface FiveStepProcess {
  Completes<Integer> queryStepCount();
  void stepOneHappened();
  void stepTwoHappened();
  void stepThreeHappened();
  void stepFourHappened();
  void stepFiveHappened();

  public static class DoStepOne extends Command { }
  public static class DoStepTwo extends Command { }
  public static class DoStepThree extends Command { }
  public static class DoStepFour extends Command { }
  public static class DoStepFive extends Command { }
}
