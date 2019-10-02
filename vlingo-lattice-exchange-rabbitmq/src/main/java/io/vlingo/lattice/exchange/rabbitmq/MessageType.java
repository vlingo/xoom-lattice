// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.rabbitmq;

/**
 * A message type indicator, either Binary or Text.
 */
public enum MessageType {
  Binary {
    @Override public boolean isBinaryListener() { return true; }
  },
  Text {
    @Override public boolean isTextListener() { return true; }
  };

  /**
   * Answers whether or not I am a binary message listener.
   * @return boolean
   */
  public boolean isBinaryListener() { return false; }

  /**
   * Answers whether or not I am a text message listener.
   * @return boolean
   */
  public boolean isTextListener() { return false; }
}
