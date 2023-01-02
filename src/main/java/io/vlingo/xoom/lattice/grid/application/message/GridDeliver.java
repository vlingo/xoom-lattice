// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application.message;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.SerializableConsumer;
import io.vlingo.xoom.wire.node.Id;

public class GridDeliver<T> implements Serializable, Message {
  private static final long serialVersionUID = 591702431591762704L;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Function<io.vlingo.xoom.actors.Message, GridDeliver<?>> from(BiConsumer<UUID, UnAckMessage> correlation, Id receiver) {
    return (message) -> {
      final LocalMessage<?> __message = (LocalMessage<?>) message;
      final Optional<Returns<?>> returns = Optional.ofNullable(__message.returns());

      final UUID answerCorrelationId = returns
              .map(_return -> UUID.randomUUID())
              .orElse(null);

      GridDeliver gridDeliver = new GridDeliver(
          __message.protocol(),
          __message.actor().address(),
          Definition.SerializationProxy.from(__message.actor().definition()),
          __message.consumer(),
          answerCorrelationId,
          __message.representation());

      if (answerCorrelationId != null) {
        correlation.accept(answerCorrelationId, new UnAckMessage(receiver, returns.get(), gridDeliver));
      }

      return gridDeliver;
    };
  }

  public final Class<T> protocol;
  public final Address address;
  public final Definition.SerializationProxy definition;
  public final SerializableConsumer<T> consumer;
  public final UUID answerCorrelationId;
  public final String representation;

  public GridDeliver(final Class<T> protocol,
                     final Address address,
                     final Definition.SerializationProxy definition,
                     final SerializableConsumer<T> consumer,
                     final String representation) {
    this(protocol, address, definition, consumer, null, representation);
  }

  public GridDeliver(final Class<T> protocol,
                     final Address address,
                     final Definition.SerializationProxy definition,
                     final SerializableConsumer<T> consumer,
                     final UUID answerCorrelationId,
                     final String representation) {
    this.protocol = protocol;
    this.address = address;
    this.definition = definition;
    this.consumer = consumer;
    this.answerCorrelationId = answerCorrelationId;
    this.representation = representation;
  }

  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format(
        "Deliver(protocol='%s', address='%s', definitionProxy='%s', consumer='%s', representation='%s')",
        protocol, address, definition, consumer, representation);
  }
}
