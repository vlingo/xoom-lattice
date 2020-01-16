package io.vlingo.lattice.grid.application;

import io.vlingo.actors.Returns;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Deliver;
import io.vlingo.lattice.grid.application.message.Start;
import io.vlingo.wire.message.RawMessage;

public interface GridActorControl {

  void start(Start start);
  void deliver(Deliver deliver);
  void answer(Answer answer);

  <T> void completeWithAnswer(Returns<T> returns);

}
