// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.State.BinaryState;

public class BinaryProjectableTest {

  @Test
  public void testProjectableness() {
    final String textState = "test-state";
    final BinaryState state =
            new BinaryState("123", String.class, 1, textState.getBytes(), 1, Metadata.with("value", "op"));
    final Projectable projectable = new BinaryProjectable(state, Collections.emptyList(), "p123");

    assertEquals("op", projectable.becauseOf()[0]);
    assertArrayEquals(textState.getBytes(), projectable.dataAsBytes());
    assertEquals("123", projectable.dataId());
    assertEquals(1, projectable.dataVersion());
    assertEquals("value", projectable.metadata());
    assertEquals("p123", projectable.projectionId());
    assertEquals(String.class.getName(), projectable.type());
    assertEquals(1, projectable.typeVersion());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testProjectableNotText() {
    final String textState = "test-state";
    final BinaryState state =
            new BinaryState("123", String.class, 1, textState.getBytes(), 1, Metadata.with("value", "op"));
    final Projectable projectable = new BinaryProjectable(state, Collections.emptyList(), "p123");
    projectable.dataAsText();
  }
}
