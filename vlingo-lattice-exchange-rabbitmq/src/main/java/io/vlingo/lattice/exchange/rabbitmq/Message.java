// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

import io.vlingo.lattice.exchange.MessageParameters;

/**
 * A base message extended by concrete messages for sending through the Exchange/Queue.
 */
public class Message {
  public final MessageParameters messageParameters;
  public final byte[] payload;

  /**
   * Constructs my default state.
   * @param payload the byte[] of the message
   * @param messageParameters the MessageParameters
   */
  public Message(final byte[] payload, final MessageParameters messageParameters) {
    this.payload = payload;
    this.messageParameters = messageParameters;
  }

  /**
   * Constructs my default state.
   * @param payload the String of the message
   * @param messageParameters the MessageParameters
   */
  public Message(final String payload, final MessageParameters messageParameters) {
    this.payload = payload.getBytes();
    this.messageParameters = messageParameters;
  }

  /**
   * Answer my payload as a String.
   * @return String
   */
  public String payloadAsText() {
    return new String(payload);
  }
}
