// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model;

import java.util.List;

import io.vlingo.actors.CompletionSupplier;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;

/**
 * An Exception used to indicate the failure of an attempt to {@code apply()}
 * state and/or {@code Source} instances.
 */
public class ApplyFailedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public final Applicable<?> applicable;

  public ApplyFailedException(final Applicable<?> applicable) {
    super();

    this.applicable = applicable;
  }

  public ApplyFailedException(final Applicable<?> applicable, final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

    this.applicable = applicable;
  }

  public ApplyFailedException(final Applicable<?> applicable, final String message, final Throwable cause) {
    super(message, cause);

    this.applicable = applicable;
  }

  public ApplyFailedException(final Applicable<?> applicable, final String message) {
    super(message);

    this.applicable = applicable;
  }

  public ApplyFailedException(final Applicable<?> applicable, final Throwable cause) {
    super(cause);

    this.applicable = applicable;
  }

  @SuppressWarnings("unchecked")
  public <T> Applicable<T> applicable() {
    return (Applicable<T>) applicable;
  }

  /**
   * The elements used in the attempted {@code apply()}.
   * @param <T> the type of the state
   */
  public static class Applicable<T> {
    public final CompletionSupplier<T> completionSupplier;
    public final Metadata metadata;
    public final List<Source<?>> sources;
    public final T state;

    public Applicable(final T state, final List<Source<?>> sources, final Metadata metadata, final CompletionSupplier<T> completionSupplier) {
      this.state = state;
      this.sources = sources;
      this.metadata = metadata;
      this.completionSupplier = completionSupplier;
    }
  }
}
