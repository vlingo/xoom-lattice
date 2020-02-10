package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.node.Id;

import java.io.Serializable;
import java.util.UUID;

public class Answer<T> implements Serializable, Message {
  private static final long serialVersionUID = -2796142731077588067L;

  public final UUID correlationId;
  public final T result;
  public final Throwable error;

  public Answer(final UUID correlationId, final T result) {
    this(correlationId, result, null);
  }

  public Answer(final UUID correlationId, final Throwable error) {
    this(correlationId, null, error);
  }

  private Answer(final UUID correlationId, final T result, final Throwable error) {
    this.correlationId = correlationId;
    this.result = result;
    this.error = error;
  }


  @Override
  public void accept(Id receiver, Id sender, Visitor visitor) {
    visitor.visit(receiver, sender, this);
  }

  @Override
  public String toString() {
    return String.format(
        "Answer(correlationId='%s', result='%s', error='%s')",
        correlationId, result, error);
  }
}
