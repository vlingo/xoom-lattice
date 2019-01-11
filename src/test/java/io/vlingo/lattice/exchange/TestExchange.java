// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import io.vlingo.common.message.Message;
import io.vlingo.common.message.MessageQueue;
import io.vlingo.common.message.MessageQueueListener;

public class TestExchange implements Exchange, MessageQueueListener {
  private final MessageQueue queue;
  private final Forwarder forwarder;

  public TestExchange(final MessageQueue queue) {
    this.queue = queue;
    queue.registerListener(this);
    this.forwarder = new Forwarder();
  }

  @Override
  public void close() {
    queue.close(true);
  }

  @Override
  public <L,E,EX> Exchange register(final Covey<L,E,EX> covey) {
    forwarder.register(covey);
    return this;
  }

  @Override
  public <L> void send(final L local) {
    System.out.println("Exchange sending: " + local);
    forwarder.forwardToSender(local);
  }

  @Override
  final public void handleMessage(final Message message) throws Exception {
    System.out.println("Exchange receiving: " + message);
    forwarder.forwardToReceiver(message);
  }
}
