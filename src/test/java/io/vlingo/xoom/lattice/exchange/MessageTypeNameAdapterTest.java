// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MessageTypeNameAdapterTest {

  private static final String INTERNAL_TYPE_NAME = "io.vlingo.xoom.test.one";
  private static final String PUBLIC_TYPE_NAME = "vlingo.xoom.test";
  private static final String SIMPLE_TYPE = ".SomeEvent";

  private static final String DOT_INTERNAL_TYPE_NAME = "io.vlingo.xoom.test.two.";
  private static final String DOT_PUBLIC_TYPE_NAME = "vlingo.xoom.test.";
  private static final String DOT_SIMPLE_TYPE = "SomeEvent";
  
  private static final String UNMATCHED_TYPE_NAME = "io.vlingo.xoom.test.unmatched.Type";
  
  private MessageTypeNameAdapter adapter;
  private Map<String, String> internalToPublic;

  @Test
  public void testThatMatchedTypeNamesRenamed() {
    final String publicTypeName = adapter.toPublicTypeName(INTERNAL_TYPE_NAME + SIMPLE_TYPE);

    assertEquals(PUBLIC_TYPE_NAME + SIMPLE_TYPE, publicTypeName);

    final String dotPublicTypeName = adapter.toPublicTypeName(DOT_INTERNAL_TYPE_NAME + DOT_SIMPLE_TYPE);

    assertEquals(DOT_PUBLIC_TYPE_NAME + DOT_SIMPLE_TYPE, dotPublicTypeName);
  }

  @Test
  public void testThatUnmatchedTypeNameNotRenamed() {
    final String publicTypeName = adapter.toPublicTypeName(UNMATCHED_TYPE_NAME);

    assertEquals(UNMATCHED_TYPE_NAME, publicTypeName);
  }

  @Before
  public void setUp() {
    internalToPublic = new HashMap<>();

    internalToPublic.put(INTERNAL_TYPE_NAME, PUBLIC_TYPE_NAME);
    internalToPublic.put(DOT_INTERNAL_TYPE_NAME, DOT_PUBLIC_TYPE_NAME);

    adapter = new MessageTypeNameAdapter(internalToPublic);
  }
}
