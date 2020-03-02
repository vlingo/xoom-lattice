package io.vlingo.lattice.grid.application.message.serialization;

import io.vlingo.lattice.grid.application.message.Encoder;
import io.vlingo.lattice.grid.application.message.Message;
import org.nustaq.serialization.FSTConfiguration;

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
