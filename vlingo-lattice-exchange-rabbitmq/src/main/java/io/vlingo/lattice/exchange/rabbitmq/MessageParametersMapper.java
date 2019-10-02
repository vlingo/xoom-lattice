// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import java.util.Date;

import com.rabbitmq.client.AMQP.BasicProperties;

import io.vlingo.lattice.exchange.MessageParameters;

/**
 * A mapper from MessageParameters to RabbitMQ BasicProperties.
 */
public class MessageParametersMapper {
  public static BasicProperties from(final MessageParameters messageParameters) {
    return new BasicProperties(
            messageParameters.contentType(),
            messageParameters.contentEncoding(),
            messageParameters.headers(),
            deliveryModeOf(messageParameters),
            priorityOf(messageParameters),
            messageParameters.correlationId(),
            messageParameters.replyTo(),
            Long.toString(messageParameters.timeToLive()),
            messageParameters.messageId(),
            new Date(messageParameters.timestamp()),
            messageParameters.typeName(),
            messageParameters.userId(),
            messageParameters.applicationId(),
            null);      // clusterId (no longer used)
  }

  private static int deliveryModeOf(final MessageParameters messageParameters) {
    return messageParameters.isDurableDeliveryMode() ? 2: 1;
  }

  private static int priorityOf(final MessageParameters messageParameters) {
    switch (messageParameters.priority()) {
    case Low:
    case P0:
      return 0;
    case P1:
      return 1;
    case P2:
      return 2;
    case P3:
      return 3;
    case Medium:
    case Normal:
    case P4:
      return 4;
    case P5:
      return 5;
    case P6:
      return 6;
    case P7:
      return 7;
    case P8:
      return 8;
    case High:
    case P9:
      return 9;
    }
    return 4;
  }
}
