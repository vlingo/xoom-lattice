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

public class CommandSourcedTest {
  @Test
  public void testThatCtorEmits() {
    final TestCommandSourcedEntity cs = new TestCommandSourcedEntity();
    assertTrue(cs.tested1);
    assertEquals(1, cs.applied().size());
    assertEquals(DoCommand1.class, cs.applied().get(0).getClass());
    assertFalse(cs.tested2);
    assertEquals(1, cs.applied().size());
  }

  @Test
  public void testThatEventEmits() {
    final TestCommandSourcedEntity cs = new TestCommandSourcedEntity();
    assertTrue(cs.tested1);
    assertFalse(cs.tested2);
    assertEquals(1, cs.applied().size());
    assertEquals(DoCommand1.class, cs.applied().get(0).getClass());
    cs.doTest2();
    assertEquals(2, cs.applied().size());
    assertEquals(DoCommand2.class, cs.applied().get(1).getClass());
  }
}
