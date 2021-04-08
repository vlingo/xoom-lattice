// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.feed;

/**
 * The identity of a {@code FeedItem}.
 */
public final class FeedItemId {
  /** The identity to use when a given FeedItem identity is not known. */
  public static final FeedItemId Unknown = new FeedItemId("");

  /** The String id value. */
  public final String value;

  /**
   * Answer a new {@code FeedItemId} having {@code id} as its {@code value}.
   * @param id the long to set as the FeedItemId value
   * @return FeedItemId
   */
  public static FeedItemId with(final long id) {
    return new FeedItemId(id);
  }

  /**
   * Construct my state.
   * @param id the long identity to assign as my value
   */
  public FeedItemId(final long id) {
    this.value = String.valueOf(id);
  }

  /**
   * Construct my state.
   * @param id the String identity to assign as my value
   */
  public FeedItemId(final String id) {
    assert(id != null);
    this.value = id;
  }

  /**
   * Answer a copy of me; a new {@code FeedItemId} with my {@code value}.
   * @return FeedItemId
   */
  public FeedItemId copy() {
    return new FeedItemId(value);
  }

  /**
   * Answer my {@code value} as a {@code long}.
   * @return long
   */
  public long toLong() {
    return Long.parseLong(value);
  }

  /**
   * Answer the next identity.
   * @return FeedItemId
   */
  public FeedItemId next() {
    return new FeedItemId(toLong() + 1L);
  }

  /**
   * Answer whether or not there is a previous identity.
   * @return boolean
   */
  public boolean hasPrevious() {
    return toLong() > 0;
  }

  /**
   * Answer the previous identity.
   * @return FeedItemId
   */
  public FeedItemId previous() {
    final long id = toLong();
    if (id == 0) {
      throw new IllegalStateException("No previous identity.");
    }
    return new FeedItemId(id - 1L);
  }

  /**
   * Answer whether or not I am the unknown identity.
   * @return boolean
   */
  public boolean isUnknown() {
    return this.equals(Unknown);
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return 31 * value.hashCode();
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || other.getClass() != FeedItemId.class) {
      return false;
    }

    return this.value.equals(((FeedItemId) other).value);
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FeedItemId[value=" + value +  "]";
  }
}
