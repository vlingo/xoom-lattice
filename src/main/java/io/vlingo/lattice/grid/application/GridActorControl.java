package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.actors.Returns;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.wire.node.Id;

import java.io.Serializable;
import java.util.List;

public interface GridActorControl {

  <T> void start(Id recipient,
                 Id sender,
                 Class<T> protocol,
                 Address address,
                 Class<? extends Actor> type,
                 Object[] parameters);

  <T> void deliver(Id recipient,
                   Id sender,
                   Returns<?> returns,
                   Class<T> protocol,
                   Address address,
                   Class<? extends Actor> type,
                   SerializableConsumer<T> consumer,
                   String representation);

  <T> void answer(Id receiver,
                  Id sender,
                  Answer<T> answer);

  void forward(Id receiver, Id sender, Message message);

  void relocate(Id receiver,
                Id sender,
                Class<? extends Actor> type,
                Address address,
                Serializable snapshot,
                List<? extends io.vlingo.actors.Message> pending);


  interface Inbound extends GridActorControl {
  }

  interface Outbound extends GridActorControl {
  }
}
