package io.vlingo.xoom.lattice.grid.application.message.serialization;

import org.nustaq.serialization.FSTConfiguration;

import io.vlingo.xoom.lattice.grid.application.message.Encoder;
import io.vlingo.xoom.lattice.grid.application.message.Message;

public class FSTEncoder implements Encoder {

  private final FSTConfiguration conf;

  public FSTEncoder(FSTConfiguration conf) {
    this.conf = conf;
  }

  @Override
  public byte[] encode(Message message) {
    return conf.asByteArray(message);
  }
}
