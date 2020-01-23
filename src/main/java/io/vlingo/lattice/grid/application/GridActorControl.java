package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Returns;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Deliver;
import io.vlingo.lattice.grid.application.message.Start;
import io.vlingo.wire.node.Id;

import java.util.function.Consumer;

public interface GridActorControl {

  <T> void start(Id host, Id ref, Class<T> protocol, Address address, Class<? extends Actor> type, Object[] parameters);

  <T> void deliver(Id host, Id ref, Class<T> protocol, Address address, String representation);

  void answer(Id host, Id ref, Answer answer);


  interface Inbound extends GridActorControl {}
  interface Outbound extends GridActorControl {}
}
