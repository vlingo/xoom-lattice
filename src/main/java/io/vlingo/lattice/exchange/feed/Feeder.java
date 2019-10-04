// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.feed;

/**
 * I provide {@code FeedItems} to a given {@code FeedInterest}.
 */
public interface Feeder {
  /**
   * Sends the {@code FeedItem} identified by {@code FeedItemId} to {@code FeedInterest}.
   * @param fromFeedItemId the FeedItemId of the FeedItem to send
   * @param feedConsumer the FeedConsumer to which the requested FeedItem is sent
   */
  void feedItemTo(final FeedItemId fromFeedItemId, final FeedConsumer feedConsumer);
}
