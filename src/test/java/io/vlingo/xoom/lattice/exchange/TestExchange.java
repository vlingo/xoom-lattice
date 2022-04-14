// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.message.Message;
import io.vlingo.xoom.common.message.MessageQueue;
import io.vlingo.xoom.common.message.MessageQueueListener;

public class TestExchange implements Exchange, MessageQueueListener {
  private AccessSafely access = AccessSafely.afterCompleting(0);

  private final MessageQueue queue;
  private final Forwarder forwarder;
  private AtomicInteger sentCount = new AtomicInteger(0);

  public TestExchange(final MessageQueue queue) {
    this.queue = queue;
    queue.registerListener(this);
    this.forwarder = new Forwarder();
    this.access = AccessSafely.afterCompleting(0);
  }

  @Override
  public void close() {
    queue.close(true);
  }

  @Override
  public <T> T channel() {
    return null;
  }

  @Override
  public <T> T connection() {
    return null;
  }

  @Override
  public String name() {
    return "TestExchange";
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
    access.writeUsing("sentCount", 1);
  }

  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely.afterCompleting(times);
    access
        .writingWith("sentCount", (Integer increment) -> sentCount.addAndGet(increment))
        .readingWith("sentCount", () -> sentCount.get());

    return access;
  }

}
