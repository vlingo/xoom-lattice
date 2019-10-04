// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.feed;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Stage;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.store.EntryReader;

/**
 * Provides support utilities for {@code Feed} and related types.
 * Every {@code Feed} has an  {@code exchangeName}.
 */
public interface Feed {
  /** The default number of messages per feed. */
  static final int DefaultMessagesPerFeedItem = 20;

  /**
   * Answer a new {@code Feed} with the given properties.
   * @param stage the Stage used to create my Feeder
   * @param exchangeName the String name of my exchange
   * @param feederType the Actor type of my Feeder
   * @param entryReaderType the EntryReader that my Feeder uses
   * @return Feed
   */
  static Feed defaultFeedWith(final Stage stage, final String exchangeName, final Class<? extends Actor> feederType, final EntryReader<?> entryReaderType) {
    return new DefaultFeed(stage, exchangeName, feederType, entryReaderType);
  }

  /**
   * Answer the {@code EntryReader<?>} to provide entries to my {@code Feeder}.
   * @return {@code EntryReader<?>}
   */
  EntryReader<?> entryReaderType();

  /**
   * Answer the {@code Class<? extends Actor>} to be used by my {@code Feeder}.
   * @return {@code Class<? extends Actor>}
   */
  Class<? extends Actor> feederType();

  /**
   * Answer my {@code Feeder} per my {@code entryReaderType()} and {@code feederType()}.
   * @return Feeder
   */
  Feeder feeder();

  /**
   * Answer the encoded identity for the {@code feedItemId}.
   * @param feedItemId the long value of the FeedItem identity
   * @return FeedItemId
   */
  default FeedItemId itemId(final long feedItemId) {
    return itemId(String.valueOf(feedItemId));
  }

  /**
   * Answer the encoded identity for the {@code feedItemId}.
   * @param feedItemId the String value of the FeedItem identity
   * @return FeedItemId
   */
  default FeedItemId itemId(final String feedItemId) {
    return new FeedItemId(feedItemId);
  }

  /**
   * Answer the type name for the message associated with the {@code entry}.
   * <p>
   * To override, the {@link Entry#type} may be used to look up an alternate name
   * @param entry the {@code Entry<?>} used to determine the type name
   * @return String
   */
  default String messageTypeNameFrom(final Entry<?> entry) {
    return entry.typeName();
  }

  /**
   * Answer the type name for the message associated with the {@code source}.
   * <p>
   * To override, the {@link Source#typeName()} may be used to look up an alternate name
   * @param source the {@code Source<?>} used to determine the type name
   * @return String
   */
  default String messageTypeNameFrom(final Source<?> source) {
    return source.typeName();
  }

  /**
   * Answer the number of messages to include in each {@code FeedItem}. Override to
   * change from the {@code DefaultMessagesPerFeedItem} default.
   * @return int
   */
  default int messagesPerFeedItem() {
    return DefaultMessagesPerFeedItem;
  }

  /**
   * Answer my {@code exchangeName}.
   * @return String
   */
  String exchangeName();
}
