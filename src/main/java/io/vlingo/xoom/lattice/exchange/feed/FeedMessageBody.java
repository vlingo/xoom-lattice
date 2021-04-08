// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.feed;

/**
 * A message body that is provided by {@code FeedMessage#body}.
 */
public class FeedMessageBody {
  /** The String value representation of the body. */
  public final String value;

  /**
   * Answer a new FeedMessageBody with {@code body}.
   * @param body the String representation of the body
   * @return FeedMessageBody
   */
  public static FeedMessageBody with(final String body) {
    return new FeedMessageBody(body);
  }

  /**
   * Construct my default state.
   * @param body the String representation to assign as my body
   */
  public FeedMessageBody(final String body) {
    assert(body != null);
    this.value = body;
  }
}
