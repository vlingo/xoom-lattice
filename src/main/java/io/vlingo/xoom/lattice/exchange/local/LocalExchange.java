// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.local;

import io.vlingo.xoom.common.message.Message;
import io.vlingo.xoom.common.message.MessageQueue;
import io.vlingo.xoom.common.message.MessageQueueListener;
import io.vlingo.xoom.lattice.exchange.Covey;
import io.vlingo.xoom.lattice.exchange.Exchange;
import io.vlingo.xoom.lattice.exchange.Forwarder;

public class LocalExchange implements Exchange, MessageQueueListener {
  private static final Object Channel = new Object();
  private static final Object Connection = new Object();

  private final MessageQueue queue;
  private final Forwarder forwarder;

  public LocalExchange(final MessageQueue queue) {
    this.queue = queue;
    queue.registerListener(this);
    this.forwarder = new Forwarder();
  }

  @Override
  public void close() {
    queue.close(true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object channel() {
    return Channel;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object connection() {
    return Connection;
  }

  @Override
  public String name() {
    return "LocalExchange";
  }

  @Override
  public <L,E,EX> Exchange register(final Covey<L,E,EX> covey) {
    forwarder.register(covey);
    return this;
  }

  @Override
  public <L> void send(final L local) {
    forwarder.forwardToSender(local);
  }

  @Override
  final public void handleMessage(final Message message) throws Exception {
    forwarder.forwardToReceiver(message);
  }
}
