// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model;

import java.util.function.Supplier;

import io.vlingo.actors.CompletesEventually;

public class CompletionSupplier<R> {
  private final Supplier<R> supplier;
  private final CompletesEventually completes;

  public static <RO> CompletionSupplier<RO> supplierOrNull(final Supplier<RO> supplier, final CompletesEventually completes) {
    if (supplier == null) {
      return null;
    }

    return new CompletionSupplier<RO>(supplier, completes);
  }

  public void complete() {
    completes.with(supplier.get());
  }

  private CompletionSupplier(final Supplier<R> supplier, final CompletesEventually completes) {
    this.supplier = supplier;
    this.completes = completes;
  }
}
