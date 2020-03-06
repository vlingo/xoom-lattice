// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.io.Serializable;

public abstract class StatelessGridActor
    extends GridActor<StatelessGridActor.Null> {

  @Override
  public void applyRelocationSnapshot(Null snapshot) { }

  @Override
  public Null provideRelocationSnapshot() { return null; }

  public interface Null extends Serializable { }
}
