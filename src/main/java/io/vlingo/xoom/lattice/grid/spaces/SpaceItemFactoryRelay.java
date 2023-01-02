// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;

import java.util.Arrays;
import java.util.Optional;

class SpaceItemFactoryRelay implements Space {
  private final Stage localStage;
  private final Space space;

  SpaceItemFactoryRelay(final Stage localStage, final Space space) {
    this.localStage = localStage;
    this.space = space;
  }

  @Override
  public <T> Completes<T> itemFor(final Class<T> protocol, final Class<? extends Actor> type, final Object... parameters) {
    final T actor = localStage.actorFor(protocol, Definition.has(type, Arrays.asList(parameters)), localStage.addressFactory().unique());
    return Completes.withSuccess(actor);
  }

  @Override
  public <T> Completes<KeyItem<T>> put(final Key key, final Item<T> item) {
    return space.put(key, item);
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> get(final Key key, final Period until) {
    return space.get(key, until);
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> take(final Key key, final Period until) {
    return space.take(key, until);
  }
}
