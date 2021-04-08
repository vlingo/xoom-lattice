// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.feed;

/**
 * A message that is provided through a feed with a {@code body} that must be a {@code Source<?>}.
 */
public final class FeedMessage {
  /** The message source body payload. */
  public final FeedMessageBody body;

  /** The unique id of the this message. */
  public final String feedMessageId;

  /** The type of this message. */
  public final String typeName;

  /** The version of the source type. */
  public final int version;

  /**
   * Answer a new {@code FeedMessage} with the given properties.
   * @param feedMessageId the String id to assign to this message
   * @param body the FeedMessageBody
   * @param typeName the String the name of the type of message
   * @param typeVersion the int version of the type of message
   * @return FeedMessage
   */
  public static FeedMessage with(final String feedMessageId, final FeedMessageBody body, final String typeName, final int typeVersion) {
    return new FeedMessage(feedMessageId, body, typeName, typeVersion);
  }

  /**
   * Construct my state with {@code feedMessageId} and {@code source}.
   * @param feedMessageId the String id to assign to this message
   * @param body the FeedMessageBody
   * @param typeName the String the name of the type of message
   * @param typeVersion the int version of the type of message
   */
  public FeedMessage(final String feedMessageId, final FeedMessageBody body, final String typeName, final int typeVersion) {
    assert(feedMessageId != null && !feedMessageId.isEmpty());
    this.feedMessageId = feedMessageId;
    assert(body != null);
    this.body = body;
    assert(typeName != null && !typeName.isEmpty());
    this.typeName = typeName;
    this.version = typeVersion;
  }
}
