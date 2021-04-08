// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.query;

import org.junit.Assert;
import org.junit.Test;

import io.vlingo.xoom.actors.CompletionTranslator;
import io.vlingo.xoom.symbio.store.QueryExpression;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ObjectQueryFailedExceptionTest {

  @Test
  public void testThatFailedHasAttempt() {
    final QueryAttempt<?,?,?> queryAttempt = new QueryAttempt(QueryAttempt.Cardinality.All, Object.class, QueryExpression.using(Object.class, ""), CompletionTranslator.translatorOrNull((o) -> null, null));
    final ObjectQueryFailedException e = new ObjectQueryFailedException(queryAttempt);

    Assert.assertNotNull(e);
    Assert.assertNotNull(e.queryAttempt);
    Assert.assertEquals(QueryAttempt.Cardinality.All, e.queryAttempt.cardinality);
    Assert.assertNotNull(e.queryAttempt.stateObjectType);
    Assert.assertNotNull(e.queryAttempt.query);
    Assert.assertNotNull(e.queryAttempt.completionTranslator);
    Assert.assertNull(e.getMessage());
    Assert.assertNull(e.getCause());
  }

  @Test
  public void testThatFailedHasExceptionInfo() {
    final Exception cause = new Exception("TestInner", new Exception());
    final QueryAttempt<?,?,?> queryAttempt = new QueryAttempt(QueryAttempt.Cardinality.All, Object.class, QueryExpression.using(Object.class, ""), CompletionTranslator.translatorOrNull((o) -> null, null));
    final ObjectQueryFailedException e = new ObjectQueryFailedException(queryAttempt, "TestOuter", cause);

    Assert.assertNotNull(e);
    Assert.assertNotNull(e.queryAttempt);
    Assert.assertEquals("TestOuter", e.getMessage());
    Assert.assertNotNull(e.getCause());
    Assert.assertEquals("TestInner", e.getCause().getMessage());
    Assert.assertNotNull(e.getMessage());
    Assert.assertNotNull(e.getCause().getCause());
    Assert.assertNull(e.getCause().getCause().getMessage());
  }
}
