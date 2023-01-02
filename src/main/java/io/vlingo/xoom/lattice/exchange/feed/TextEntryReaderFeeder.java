// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.feed;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.symbio.BaseEntry.TextEntry;
import io.vlingo.xoom.symbio.store.EntryReader;

/**
 * The {@code Feeder} serving {@code TextEntry} instances.
 */
public class TextEntryReaderFeeder extends Actor implements Feeder {
  private final EntryReader<TextEntry> entryReader;
  private final Feed feed;

  /**
   * Construct my default state.
   * @param feed the Feed that I serve
   * @param entryReader the {@code EntryReader<TextEntry>} from which content is read
   */
  public TextEntryReaderFeeder(final Feed feed, final EntryReader<TextEntry> entryReader) {
    this.feed = feed;
    this.entryReader = entryReader;
  }

  /**
   * @see io.vlingo.xoom.lattice.exchange.feed.Feeder#feedItemTo(io.vlingo.xoom.lattice.exchange.feed.FeedItemId, io.vlingo.xoom.lattice.exchange.feed.FeedConsumer)
   */
  @Override
  public void feedItemTo(final FeedItemId feedItemId, final FeedConsumer feedInterest) {
    final long feedId = feedItemId.toLong();
    final long id = (feedId - 1L) * feed.messagesPerFeedItem() + 1;

    entryReader
      .readNext(String.valueOf(id), feed.messagesPerFeedItem())
      .andThen(entries -> {
        feedInterest.consumeFeedItem(toFeedItem(feedItemId, entries));
        return entries;
      });
  }

  /**
   * Answer a new {@code FeedItem} from converted {@code entries}.
   * @param feedItemId the FeedItemId of the current item
   * @param entries the List<TextEntry> to convert
   * @return FeedItem
   */
  private FeedItem toFeedItem(final FeedItemId feedItemId, final List<TextEntry> entries) {
    final List<FeedMessage> messages = new ArrayList<>(entries.size());
    for (final TextEntry entry : entries) {
      final FeedMessageBody body = FeedMessageBody.with(entry.entryData());
      final FeedMessage message = FeedMessage.with(entry.id(), body, entry.typeName(), entry.typeVersion());
      messages.add(message);
    }

    if (feed.messagesPerFeedItem() == entries.size()) {
      return FeedItem.archivedFeedItemWith(feedItemId, feedItemId.next(), feedItemId.previous(), messages);
    } else {
      return FeedItem.currentFeedWith(feedItemId, feedItemId.previous(), messages);
    }
  }
}
