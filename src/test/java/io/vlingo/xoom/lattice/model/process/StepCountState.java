// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

public class StepCountState {
  private int stepCount;

  public StepCountState(int stepCount) {
    this.stepCount = stepCount;
  }

  public StepCountState() {
    this.stepCount = 0;
  }

  public void countStep() {
    ++this.stepCount;
  }

  public int stepCount() {
    return this.stepCount;
  }
}
