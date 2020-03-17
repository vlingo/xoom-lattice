package io.vlingo.lattice.grid.example;

import io.vlingo.common.Completes;

public interface PingPongReferee {
  Completes<Pinger> whistle(final String name);
}
