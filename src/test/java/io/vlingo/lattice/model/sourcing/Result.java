// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vlingo.actors.testkit.AccessSafely;

public class Result {
  private AccessSafely access;
  private CopyOnWriteArrayList<Object> applied = new CopyOnWriteArrayList<>();
  private AtomicBoolean tested1 = new AtomicBoolean(false);
  private AtomicBoolean tested2 = new AtomicBoolean(false);
  private AtomicBoolean tested3 = new AtomicBoolean(false);
  
  public Result() {
    super();
    access = afterCompleting(0);
  }

  public AccessSafely access() {
    return access;
  }
  
  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely
      .afterCompleting(times)
      
      .writingWith("applied", (Object obj) -> applied.add(obj))
      .readingWith("applied", () -> applied)
      .readingWith("appliedCount", () -> applied.size())
      .readingWith("appliedAt", (Integer index) -> applied.get(index))
      
      .writingWith("tested1", (Boolean trueOrFalse) -> tested1.set(trueOrFalse))
      .readingWith("tested1", () -> tested1.get())
      .writingWith("tested2", (Boolean trueOrFalse) -> tested2.set(trueOrFalse))
      .readingWith("tested2", () -> tested2.get())
      .writingWith("tested3", (Boolean trueOrFalse) -> tested3.set(trueOrFalse))
      .readingWith("tested3", () -> tested3.get());

    return access;
  }
}
