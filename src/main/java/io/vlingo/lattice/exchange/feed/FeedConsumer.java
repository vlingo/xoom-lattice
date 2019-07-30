// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.feed;

/**
 * The protocol used by parties interested in consuming a {@code FeedItem}
 * stream provided by a {@code Feeder}.
 */
public interface FeedConsumer {

  /**
   * Consumes the {@code feedItem} requested of the {@code Feeder#feedItemTo(FeedItemId, FeedConsumer)}.
   * @param feedItem the FeedItem requested of the Feeder to be consumed
   */
  void consumeFeedItem(final FeedItem feedItem);
}
