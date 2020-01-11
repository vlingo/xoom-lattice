// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.feed;

import java.util.Collections;
import java.util.List;

/**
 * A single feed item of messages including links to next and previous items.
 * If considered archived my {@code archived} indicator will be {@code true}.
 * An archived feed item is one that will not change in the future; it is full.
 */
public final class FeedItem {
  /** My indicator of whether I am considered archived or not. */
  public final boolean archived;

  /** My identity. */
  public final FeedItemId id;

  /** The identity of the next entry, if known. */
  public final FeedItemId nextId;

  /** The identity of the previous entry, if any. */
  public final FeedItemId previousId;

  /** My messages. */
  public final List<FeedMessage> messages;

  /**
   * Answer the new {@code FeedItem} that is considered archived (immutable), with
   * {@code id}, {@code nextId}, {@code previousId}, and {@code messages}.
   * @param id the FeedItemId identity of the FeedItem
   * @param nextId the FeedItemId identity of the next FeedItem
   * @param previousId the FeedItemId identity of the previous FeedItem
   * @param messages the {@code List<FeedMessage>} of the FeedItem
   * @return FeedItem
   */
  public static FeedItem archivedFeedItemWith(final FeedItemId id, final FeedItemId nextId, final FeedItemId previousId, final List<FeedMessage> messages) {
    return new FeedItem(id, nextId, previousId, messages, true);
  }

  /**
   * Answer the new {@code FeedItem} that is considered current/head, with
   * {@code id}, a {@code previousId}, and {@code messages}.
   * <p>
   * Note that there is no {@code nextId} yet available because this is the current/head.
   * @param id the FeedItemId identity of the FeedItem
   * @param previousId the FeedItemId identity of the previous FeedItem
   * @param messages the {@code List<FeedMessage>} of the FeedItem
   * @return FeedItem
   */
  public static FeedItem currentFeedWith(final FeedItemId id, final FeedItemId previousId, final List<FeedMessage> messages) {
    return new FeedItem(id, previousId, messages);
  }

  /**
   * Construct my state with identities, messages, and archive indicator.
   * @param id my FeedItemId identity
   * @param nextId the FeedItemId identity of the next FeedItem, if known
   * @param previousId the FeedItemId identity of the previous FeedItem, if any
   * @param messages the {@code List<FeedMessage>} of messages
   * @param archived the boolean indicator that this feed is considered archived or not
   */
  private FeedItem(final FeedItemId id, final FeedItemId nextId, final FeedItemId previousId, final List<FeedMessage> messages, final boolean archived) {
    assert(id != null);
    this.id = id;
    assert(nextId != null);
    this.nextId = nextId;
    assert(previousId != null);
    this.previousId = previousId;
    assert(messages != null);
    this.messages = Collections.unmodifiableList(messages);
    this.archived = archived;
  }

  /**
   * Construct my state with identities, messages, and {@code false} archive indicator.
   * @param id my FeedItemId identity
   * @param previousFeedId the String identity of the previous feed, if any
   * @param messages the {@code List<FeedMessage>} of messages
   */
  private FeedItem(final FeedItemId id, final FeedItemId previousFeedId, final List<FeedMessage> messages) {
    this(id, FeedItemId.Unknown, previousFeedId, messages, false);
  }
}
