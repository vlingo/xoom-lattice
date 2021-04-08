package io.vlingo.xoom.lattice.grid.application.message.serialization;

import io.vlingo.xoom.lattice.grid.application.message.Decoder;
import io.vlingo.xoom.lattice.grid.application.message.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public final class JavaObjectDecoder implements Decoder {

  @Override
  public Message decode(byte[] bytes) {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    try (ObjectInputStream in = new ObjectInputStream(bis)) {
      return (Message) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalArgumentException("decode failed", e);
    }
  }
}
