// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.State.TextState;

public class ProjectableTest {

  @Test
  public void testThatNoStateFailsSafely() {
    final Projectable projectable = new TextProjectable(null, Arrays.asList(), "123");

    Assert.assertEquals(-1, projectable.dataVersion());
    Assert.assertEquals("", projectable.dataId());
    Assert.assertEquals("", projectable.metadata());
    Assert.assertFalse(projectable.hasObject());
    Assert.assertNull(projectable.object());
    Assert.assertFalse(projectable.optionalObject().isPresent());
    Assert.assertFalse(projectable.hasState());
    Assert.assertEquals(-1, projectable.typeVersion());
  }

  @Test
  public void testThatStateDoesNotFail() {
    final Object object = new Object();
    final Projectable projectable =
            new TextProjectable(
                    new TextState("ABC", String.class, 1, "state", 1, Metadata.with(object, "value", "op1")),
                    Arrays.asList(),
                    "123");

    Assert.assertEquals(1, projectable.dataVersion());
    Assert.assertEquals("ABC", projectable.dataId());
    Assert.assertEquals("value", projectable.metadata());
    Assert.assertTrue(projectable.hasObject());
    Assert.assertNotNull(projectable.object());
    Assert.assertEquals(object, projectable.object());
    Assert.assertTrue(projectable.optionalObject().isPresent());
    Assert.assertTrue(projectable.hasState());
    Assert.assertEquals(1, projectable.typeVersion());
  }
}
