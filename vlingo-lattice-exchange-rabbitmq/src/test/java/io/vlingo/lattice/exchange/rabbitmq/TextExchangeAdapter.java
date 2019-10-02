// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import io.vlingo.lattice.exchange.ExchangeAdapter;
import io.vlingo.lattice.exchange.ExchangeMapper;
import io.vlingo.lattice.exchange.MessageParameters;
import io.vlingo.lattice.exchange.MessageParameters.DeliveryMode;

public class TextExchangeAdapter implements ExchangeAdapter<String,String,Message> {
  private ExchangeMapper<String,String> mapper = new TextExchangeMapper();

  @Override
  public String fromExchange(final Message exchangeMessage) {
    final String local = mapper.externalToLocal(exchangeMessage.payloadAsText());
    return local;
  }

  @Override
  public Message toExchange(final String localMessage) {
    return new Message(localMessage, MessageParameters.bare().deliveryMode(DeliveryMode.Durable));
  }

  @Override
  public boolean supports(final Object exchangeMessage) {
    return Message.class == exchangeMessage.getClass();
  }
}
