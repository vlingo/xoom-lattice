package io.vlingo.lattice.grid.application;

import io.vlingo.lattice.grid.application.message.*;
import io.vlingo.wire.message.RawMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.function.Consumer;

public class ApplicationMessageHandler {

  private final GridActorControl control;

  public ApplicationMessageHandler(GridActorControl control) {
    this.control = control;
  }

  public void handle(RawMessage raw) {
    ByteArrayInputStream bais = new ByteArrayInputStream(raw.asBinaryMessage());
    try {
      ObjectInputStream ois = new ObjectInputStream(bais);
      Message message = (Message) ois.readObject();

      if(message instanceof Start) {
        handle((Start) message);
      }
      else if (message instanceof Deliver) {
        handle((Deliver) message);
      }
      else if (message instanceof Answer) {
        handle((Answer) message);
      }
      else {
        throw new UnsupportedOperationException("Unhandled message type: " + message.getClass().getName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handle(Start start) {
    control.start(start);
  }

  private void handle(Deliver deliver) {
    control.deliver(deliver);
  }

  private void handle(Answer answer) {
    control.answer(answer);
  }
}
