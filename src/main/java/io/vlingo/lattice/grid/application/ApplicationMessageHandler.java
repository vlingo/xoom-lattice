package io.vlingo.lattice.grid.application;

import io.vlingo.wire.message.RawMessage;

public interface ApplicationMessageHandler {

  void handle(RawMessage message);
}
