// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EventSourcedTest {
  @Test
  public void testThatCtorEmits() {
    final TestEventSourcedEntity es = new TestEventSourcedEntity();
    assertTrue(es.tested1);
    assertEquals(1, es.applied().size());
    assertEquals(Test1Happened.class, es.applied().get(0).getClass());
    assertFalse(es.tested2);
    assertEquals(1, es.applied().size());
  }

  @Test
  public void testThatCommandEmits() {
    final TestEventSourcedEntity es = new TestEventSourcedEntity();
    assertTrue(es.tested1);
    assertFalse(es.tested2);
    assertEquals(1, es.applied().size());
    assertEquals(Test1Happened.class, es.applied().get(0).getClass());
    es.doTest2();
    assertEquals(2, es.applied().size());
    assertEquals(Test2Happened.class, es.applied().get(1).getClass());
  }
}
