// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.feed;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.actors.testkit.AccessSafely;

public class MockFeedConsumer implements FeedConsumer {
  private AccessSafely access;
  public final Map<Long,FeedItem> feedItems = new ConcurrentHashMap<>();

  @Override
  public void consumeFeedItem(final FeedItem feedItem) {
    access.writeUsing("feedItems", feedItem);
  }

  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely
      .afterCompleting(times)

      .writingWith("feedItems", (FeedItem feedItem) -> feedItems.put(feedItem.id.toLong(), feedItem))
      .readingWith("feedItems", () -> feedItems);

    return access;
  }
}
