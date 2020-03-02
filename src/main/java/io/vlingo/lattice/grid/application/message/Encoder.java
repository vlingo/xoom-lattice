package io.vlingo.lattice.grid.application.message;

public interface Encoder {
  byte[] encode(Message message);
}
