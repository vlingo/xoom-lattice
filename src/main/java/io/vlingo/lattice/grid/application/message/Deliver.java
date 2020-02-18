package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Returns;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.wire.node.Id;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Deliver<T> implements Serializable, Message {
  private static final long serialVersionUID = 591702431591762704L;

  public static Function<io.vlingo.actors.Message, Deliver<?>> from(BiConsumer<UUID, Returns<?>> correlation) {
    return (message) -> {
      final LocalMessage<?> __message = (LocalMessage<?>) message;
      final Optional<Returns<?>> returns = Optional.ofNullable(__message.returns());

      final UUID answerCorrelationId = returns.map(_return -> {
        final UUID correlationId = UUID.randomUUID();
        correlation.accept(correlationId, _return);
        return correlationId;
      }).orElse(null);

      return new Deliver(
          __message.protocol(),
          __message.actor().address(),
          __message.actor().getClass(),
          __message.consumer(),
          answerCorrelationId,
          __message.representation());
    };
  }

  public final Class<T> protocol;
  public final Address address;
  public final Class<? extends Actor> type;
  public final SerializableConsumer<T> consumer;
  public final UUID answerCorrelationId;
  public final String representation;

  public Deliver(final Class<T> protocol,
                 final Address address,
                 Class<? extends Actor> type,
                 final SerializableConsumer<T> consumer,
                 final String representation) {
    this(protocol, address, type, consumer, null, representation);
  }

  public Deliver(final Class<T> protocol,
                 final Address address,
                 Class<? extends Actor> type,
                 final SerializableConsumer<T> consumer,
                 final UUID answerCorrelationId,
                 final String representation) {
    this.protocol = protocol;
    this.address = address;
    this.type = type;
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
        "Deliver(protocol='%s', address='%s', consumer='%s', representation='%s')",
        protocol, address, consumer, representation);
  }
}
