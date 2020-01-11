// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import io.vlingo.symbio.BaseEntry.TextEntry;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State.TextState;

public class TextProjectableTest {

  @Test
  public void testProjectableness() {
    final String textState = "test-state";
    final TextState state =
            new TextState("123", String.class, 1, textState, 1, Metadata.with("value", "op"));
    final Projectable projectable = new TextProjectable(state, Collections.emptyList(), "p123");

    assertEquals("op", projectable.becauseOf()[0]);
    assertEquals(textState, projectable.dataAsText());
    assertEquals("123", projectable.dataId());
    assertEquals(1, projectable.dataVersion());
    assertEquals("value", projectable.metadata());
    assertEquals("p123", projectable.projectionId());
    assertEquals(String.class.getName(), projectable.type());
    assertEquals(1, projectable.typeVersion());
  }

  @Test
  public void testStateWithEventProjectableness() {
    final String textState = "test-state";
    final TextState state =
            new TextState("123", String.class, 1, textState, 1, Metadata.with("value", "op"));
    final Entry<String> entry = new TextEntry();
    final Projectable projectable = new TextProjectable(state, Arrays.asList(entry), "p123");

    assertEquals("op", projectable.becauseOf()[0]);
    assertEquals("java.lang.Object", projectable.becauseOf()[1]);
    assertEquals(textState, projectable.dataAsText());
    assertEquals("123", projectable.dataId());
    assertEquals(1, projectable.dataVersion());
    assertEquals("value", projectable.metadata());
    assertEquals("p123", projectable.projectionId());
    assertEquals(String.class.getName(), projectable.type());
    assertEquals(1, projectable.typeVersion());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testProjectableNotBytes() {
    final String textState = "test-state";
    final TextState state =
            new TextState("123", String.class, 1, textState, 1, Metadata.with("value", "op"));
    final Projectable projectable = new TextProjectable(state, Collections.emptyList(), "p123");
    projectable.dataAsBytes();
  }
}
