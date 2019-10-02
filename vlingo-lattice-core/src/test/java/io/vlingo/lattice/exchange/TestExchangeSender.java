// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import io.vlingo.common.message.MessageQueue;

public class TestExchangeSender implements ExchangeSender<ExchangeMessage> {
  public final MessageQueue queue;

  public TestExchangeSender(final MessageQueue queue) {
    this.queue = queue;
  }

  @Override
  public void send(final ExchangeMessage message) {
    System.out.println("MessageQueue sending: " + message);
    queue.enqueue(message);
  }
}
