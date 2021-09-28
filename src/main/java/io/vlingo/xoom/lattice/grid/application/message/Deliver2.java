// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application.message;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.wire.node.Id;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Function;

public class Deliver2<T> implements Serializable, Message {

  public final Class<T> protocol;
  public final Function<Grid, Actor> actorProvider;
  public final SerializableConsumer<T> consumer;
  public final String representation;
  public final UUID answerCorrelationId;

  public Deliver2(Class<T> protocol, Function<Grid, Actor> actorProvider, SerializableConsumer<T> consumer, String representation) {
    this(protocol, actorProvider, consumer, representation, null);
  }

  public Deliver2(Class<T> protocol, Function<Grid, Actor> actorProvider, SerializableConsumer<T> consumer, String representation, UUID answerCorrelationId) {
    this.protocol = protocol;
    this.actorProvider = actorProvider;
    this.consumer = consumer;
    this.representation = representation;
    this.answerCorrelationId = answerCorrelationId;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format("Space(protocol='%s', actorProvider='%s', consumer='%s', representation='%s'",
            protocol, actorProvider, consumer, representation);
  }
}

