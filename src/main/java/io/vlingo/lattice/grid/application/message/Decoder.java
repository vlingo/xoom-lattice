package io.vlingo.lattice.grid.application.message;

public interface Decoder {
  Message decode(byte[] bytes);
}
