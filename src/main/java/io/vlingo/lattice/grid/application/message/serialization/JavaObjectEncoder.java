package io.vlingo.lattice.grid.application.message.serialization;

import io.vlingo.lattice.grid.application.message.Encoder;
import io.vlingo.lattice.grid.application.message.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public final class JavaObjectEncoder implements Encoder {

  @Override
  public byte[] encode(Message message) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(message);
      out.flush();
      return bos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("encode failed", e);
    }
  }
}
