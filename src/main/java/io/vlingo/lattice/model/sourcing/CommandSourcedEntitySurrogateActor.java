// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import io.vlingo.actors.Actor;

public class CommandSourcedEntitySurrogateActor extends Actor implements CommandSourcedEntitySurrogate {
  private final CommandSourced sourced;

  public CommandSourcedEntitySurrogateActor(final CommandSourced sourced) {
    this.sourced = sourced;
  }

  @Override
  public void handle(final EventStimulus stimulus) {

  }
}
