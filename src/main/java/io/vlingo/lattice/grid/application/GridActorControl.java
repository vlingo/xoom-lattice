package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.wire.node.Id;

public interface GridActorControl {

  <T> void start(
      Id recipient,
      Id sender,
      Class<T> protocol,
      Address address,
      Class<? extends Actor> type,
      Object[] parameters);

  <T> void deliver(
      Id recipient,
      Id sender,
      Class<T> protocol,
      Address address,
      SerializableConsumer<T> consumer,
      String representation);

  void answer(Id host, Id ref, Answer answer);

  void forward(Id recipient, Id sender, Message message);


  interface Inbound extends GridActorControl {
  }

  interface Outbound extends GridActorControl {
  }
}
