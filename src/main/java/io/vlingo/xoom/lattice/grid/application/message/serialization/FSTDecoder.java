package io.vlingo.xoom.lattice.grid.application.message.serialization;

import io.vlingo.xoom.lattice.grid.application.message.Decoder;
import io.vlingo.xoom.lattice.grid.application.message.Message;
import org.nustaq.serialization.FSTConfiguration;

public class FSTDecoder implements Decoder {

  private final FSTConfiguration conf;

  public FSTDecoder(FSTConfiguration conf) {
    this.conf = conf;
  }

  @Override
  public Message decode(byte[] bytes) {
    return (Message)conf.asObject(bytes);
  }
}
