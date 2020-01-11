// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.feed;

/**
 * Holder of Feed exchange and subscriber position information.
 */
public final class FeedPosition {
  /** The String name of the exchange. */
  public final String exchangeName;

  /** The identity indicating the current position.  */
  public final FeedItem feedItemId;

  /** The String identity of the subscriber. */
  public final String subscriberId;

  /**
   * Answer a new FeedPosition.
   * @param exchangeName the String name of the exchange
   * @param subscriberId the String identity of the subscriber
   * @param feedItemId the FeedItemId indicating the current position
   * @return FeedPosition
   */
  public FeedPosition is(final String exchangeName, final String subscriberId, final FeedItem feedItemId) {
    return new FeedPosition(exchangeName, subscriberId, feedItemId);
  }

  /**
   * Construct my state.
   * @param exchangeName the String name of the exchange
   * @param subscriberId the String name of the subscriber
   * @param feedItemId the FeedItemId indicating the current position
   */
  public FeedPosition(final String exchangeName, final String subscriberId, final FeedItem feedItemId) {
    assert(exchangeName != null && !exchangeName.isEmpty());
    this.exchangeName = exchangeName;
    assert(subscriberId != null && !subscriberId.isEmpty());
    this.subscriberId = subscriberId;
    this.feedItemId = feedItemId;
  }

  /**
   * Answer a new {@code FeedPosition} with {@code feedItemId}.
   * @param feedItemId the FeedItemId indicating the current position
   * @return FeedPosition
   */
  public FeedPosition with(final FeedItem feedItemId) {
    return new FeedPosition(exchangeName, subscriberId, feedItemId);
  }
}
