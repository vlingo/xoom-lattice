// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.vlingo.lattice.exchange.ConnectionSettings;
import io.vlingo.lattice.exchange.Exchange;

public class ExchangeFactoryTest {

  @Test
  public void testThatFanOutInstanceConnects() {
    final Exchange exchange = ExchangeFactory.fanOutInstance(settings(), "test-fanout", true);
    assertNotNull(exchange);
    assertEquals("test-fanout", exchange.name());
    exchange.close();
  }

  private ConnectionSettings settings() {
    return ConnectionSettings.instance("localhost", ConnectionSettings.UndefinedPort, "/", "guest", "guest");
  }
}
