// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.local;

import io.vlingo.xoom.common.message.MessageQueue;
import io.vlingo.xoom.lattice.exchange.ExchangeSender;

public class LocalExchangeSender implements ExchangeSender<LocalExchangeMessage> {
  public final MessageQueue queue;

  public LocalExchangeSender(final MessageQueue queue) {
    this.queue = queue;
  }

  @Override
  public void send(final LocalExchangeMessage message) {
    queue.enqueue(message);
  }
}
