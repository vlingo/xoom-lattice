// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.vlingo.lattice.model.ApplyFailedException.Applicable;
import io.vlingo.symbio.Metadata;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ApplyFailedExceptionTest {

  @Test
  public void testThatFailedHasApplicable() {
    final Applicable<?> applicable = new Applicable(null, Arrays.asList(), Metadata.nullMetadata(), CompletionSupplier.supplierOrNull(() -> null, null));
    final ApplyFailedException e = new ApplyFailedException(applicable);

    Assert.assertNotNull(e);
    Assert.assertNotNull(e.applicable);
    Assert.assertNull(e.applicable.state);
    Assert.assertNotNull(e.applicable.sources);
    Assert.assertNotNull(e.applicable.metadata);
    Assert.assertNotNull(e.applicable.completionSupplier);
    Assert.assertNull(e.getMessage());
    Assert.assertNull(e.getCause());
  }

  @Test
  public void testThatFailedHasExceptionInfo() {
    final Exception cause = new Exception("TestInner", new Exception());
    final Applicable<?> applicable = new Applicable(null, Arrays.asList(), Metadata.nullMetadata(), CompletionSupplier.supplierOrNull(() -> null, null));
    final ApplyFailedException e = new ApplyFailedException(applicable, "TestOuter", cause);

    Assert.assertNotNull(e);
    Assert.assertNotNull(e.applicable);
    Assert.assertEquals("TestOuter", e.getMessage());
    Assert.assertNotNull(e.getCause());
    Assert.assertEquals("TestInner", e.getCause().getMessage());
    Assert.assertNotNull(e.getMessage());
    Assert.assertNotNull(e.getCause().getCause());
    Assert.assertNull(e.getCause().getCause().getMessage());
  }
}
