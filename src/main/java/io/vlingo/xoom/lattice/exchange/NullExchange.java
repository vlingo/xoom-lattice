// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

import io.vlingo.xoom.actors.Logger;

/**
 * Exchange that does nothing.
 */
public class NullExchange implements Exchange {
  public static final NullExchange Instance = new NullExchange();

  private final Logger logger = Logger.basicLogger();

  /**
   * @see io.vlingo.xoom.lattice.exchange.Exchange#close()
   */
  @Override
  public void close() { }

  /**
   * @see io.vlingo.xoom.lattice.exchange.Exchange#channel()
   */
  @Override
  public <T> T channel() { return null; }

  /**
   * @see io.vlingo.xoom.lattice.exchange.Exchange#connection()
   */
  @Override
  public <T> T connection() { return null; }

  /**
   * @see io.vlingo.xoom.lattice.exchange.Exchange#name()
   */
  @Override
  public String name() { return "NullExchange"; }

  /**
   * @see io.vlingo.xoom.lattice.exchange.Exchange#register(io.vlingo.xoom.lattice.exchange.Covey)
   */
  @Override
  public <L, E, EX> Exchange register(final Covey<L, E, EX> covey) { return this; }

  /**
   * @see io.vlingo.xoom.lattice.exchange.Exchange#send(java.lang.Object)
   */
  @Override
  public <L> void send(final L local) {
    logger.error("NullExchange: Sending nowhere: " + local);
  }
}
