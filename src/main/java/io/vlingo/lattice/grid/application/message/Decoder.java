package io.vlingo.lattice.grid.application.message;

import io.vlingo.wire.message.RawMessage;

public interface Decoder {
  Message decode(byte[] bytes);
}
