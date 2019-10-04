// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.symbio.store.object.StateObject;

import java.util.concurrent.atomic.AtomicLong;

public class StepCountObjectState extends StateObject implements Comparable<StepCountObjectState> {
  private static final long serialVersionUID = 1L;
  private static final AtomicLong identityGenerator = new AtomicLong(0);

  private int stepCount;

  public StepCountObjectState() {
    super(identityGenerator.incrementAndGet());

    this.stepCount = 0;
  }

  public void countStep() {
    ++this.stepCount;
  }

  public int stepCount() {
    return this.stepCount;
  }

  @Override
  public int compareTo(final StepCountObjectState other) {
    return Long.compare(this.persistenceId(), other.persistenceId());
  }
}
