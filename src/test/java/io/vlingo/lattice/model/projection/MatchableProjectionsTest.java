// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MatchableProjectionsTest {

  @Test
  public void testThatEntireCauseMatches() {
    final MatchableProjections matchable = new MatchableProjections();

    matchable.mayDispatchTo(new MockProjection(), "some-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "some-other-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "yet-another-matching-text");

    assertEquals(1, matchable.matchProjections("some-matching-text").size());
    assertEquals(1, matchable.matchProjections("some-other-matching-text").size());
    assertEquals(1, matchable.matchProjections("yet-another-matching-text").size());
  }

  @Test
  public void testThatBeginsWithCauseMatches() {
    final MatchableProjections matchable = new MatchableProjections();

    matchable.mayDispatchTo(new MockProjection(), "some-matching-*");
    matchable.mayDispatchTo(new MockProjection(), "some-mat*");
    matchable.mayDispatchTo(new MockProjection(), "some-other-matching-*");
    matchable.mayDispatchTo(new MockProjection(), "some-other-*");
    matchable.mayDispatchTo(new MockProjection(), "some-*");
    matchable.mayDispatchTo(new MockProjection(), "yet-another-matching-*");
    matchable.mayDispatchTo(new MockProjection(), "yet-another-*");
    matchable.mayDispatchTo(new MockProjection(), "yet-*");
    matchable.mayDispatchTo(new MockProjection(), "yet*"); // note matches whole text "yet"

    assertEquals(3, matchable.matchProjections("some-matching-text").size());
    assertEquals(3, matchable.matchProjections("some-other-matching-text").size());
    assertEquals(4, matchable.matchProjections("yet-another-matching-text").size());
    assertEquals(1, matchable.matchProjections("yet").size()); // matched with "yet*"
  }

  @Test
  public void testThatEndsWithCauseMatches() {
    final MatchableProjections matchable = new MatchableProjections();

    matchable.mayDispatchTo(new MockProjection(), "*-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "*-text");
    matchable.mayDispatchTo(new MockProjection(), "*-other-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "*-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "*-text");
    matchable.mayDispatchTo(new MockProjection(), "*-another-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "*-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "*-text");
    matchable.mayDispatchTo(new MockProjection(), "*text"); // note matches whole text "text"

    assertEquals(7, matchable.matchProjections("some-matching-text").size());
    assertEquals(8, matchable.matchProjections("some-other-matching-text").size());
    assertEquals(8, matchable.matchProjections("yet-another-matching-text").size());
    assertEquals(1, matchable.matchProjections("text").size()); // matched with "text*"
  }

  @Test
  public void testThatContainsCauseMatches() {
    final MatchableProjections matchable = new MatchableProjections();

    matchable.mayDispatchTo(new MockProjection(), "*-matching-*");
    matchable.mayDispatchTo(new MockProjection(), "*-other-matching-*");
    matchable.mayDispatchTo(new MockProjection(), "*-another-matching-*");
    matchable.mayDispatchTo(new MockProjection(), "*-*");
    matchable.mayDispatchTo(new MockProjection(), "*text*");

    assertEquals(3, matchable.matchProjections("some-matching-text").size());
    assertEquals(4, matchable.matchProjections("some-other-matching-text").size());
    assertEquals(4, matchable.matchProjections("yet-another-matching-text").size());
    assertEquals(1, matchable.matchProjections("text").size()); // matched with "text*"
  }

  @Test
  public void testThatNothingMatches() {
    final MatchableProjections matchable = new MatchableProjections();

    matchable.mayDispatchTo(new MockProjection(), "other-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "another-matching-text");
    matchable.mayDispatchTo(new MockProjection(), "matching-text");
    matchable.mayDispatchTo(new MockProjection(), "text");

    assertEquals(0, matchable.matchProjections("some-matching-text").size());
    assertEquals(0, matchable.matchProjections("some-other-matching-text").size());
    assertEquals(0, matchable.matchProjections("yet-another-matching-text").size());
    assertEquals(0, matchable.matchProjections("another").size());
    assertEquals(0, matchable.matchProjections("other").size());
    assertEquals(0, matchable.matchProjections("matching").size());
  }
}
