package io.vlingo.lattice.grid.application.message;

import io.vlingo.actors.Address;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.wire.node.Id;

import java.io.Serializable;
import java.util.UUID;

public class Deliver<T> implements Serializable, Message {
  private static final long serialVersionUID = 591702431591762704L;

  public final Class<T> protocol;
  public final Address address;
  public final SerializableConsumer<T> consumer;
  public final UUID answerCorrelationId;
  public final String representation;

  public Deliver(final Class<T> protocol,
                 final Address address,
                 final SerializableConsumer<T> consumer,
                 final String representation) {
    this(protocol, address, consumer, null, representation);
  }

  public Deliver(final Class<T> protocol,
                 final Address address,
                 final SerializableConsumer<T> consumer,
                 final UUID answerCorrelationId,
                 final String representation) {
    this.protocol = protocol;
    this.address = address;
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
