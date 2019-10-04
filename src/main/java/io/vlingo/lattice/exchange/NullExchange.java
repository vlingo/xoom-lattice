// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import io.vlingo.actors.Logger;

/**
 * Exchange that does nothing.
 */
public class NullExchange implements Exchange {
  public static final NullExchange Instance = new NullExchange();

  private final Logger logger = Logger.basicLogger();

  /**
   * @see io.vlingo.lattice.exchange.Exchange#close()
   */
  @Override
  public void close() { }

  /**
   * @see io.vlingo.lattice.exchange.Exchange#channel()
   */
  @Override
  public <T> T channel() { return null; }

  /**
   * @see io.vlingo.lattice.exchange.Exchange#connection()
   */
  @Override
  public <T> T connection() { return null; }

  /**
   * @see io.vlingo.lattice.exchange.Exchange#name()
   */
  @Override
  public String name() { return "NullExchange"; }

  /**
   * @see io.vlingo.lattice.exchange.Exchange#register(io.vlingo.lattice.exchange.Covey)
   */
  @Override
  public <L, E, EX> Exchange register(final Covey<L, E, EX> covey) { return this; }

  /**
   * @see io.vlingo.lattice.exchange.Exchange#send(java.lang.Object)
   */
  @Override
  public <L> void send(final L local) {
    logger.error("NullExchange: Sending nowhere: " + local);
  }
}
