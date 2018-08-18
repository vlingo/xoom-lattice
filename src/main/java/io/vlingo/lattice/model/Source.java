// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.vlingo.common.version.SemanticVersion;

public abstract class Source<T> {
  public final long dateTimeSourced;
  public final int sourceTypeVersion;

  public static <T> Source<T> nulled() {
    return new NullSource<T>();
  }

  @SafeVarargs
  public static <T> List<Source<T>> all(final Source<T>... sources) {
    return all(Arrays.asList(sources));
  }

  public static <T> List<Source<T>> all(final List<Source<T>> sources) {
    final List<Source<T>> all = new ArrayList<>(sources.size());

    for (final Source<T> source : sources) {
      if (!source.isNull()) {
        all.add(source);
      }
    }
    return all;
  }

  public static <T> List<Source<T>> none() {
    return Collections.emptyList();
  }

  public boolean isNull() {
    return false;
  }

  protected Source() {
    this(SemanticVersion.toValue(1, 0, 0));
  }

  protected Source(final int sourceTypeVersion) {
    this.dateTimeSourced = System.currentTimeMillis();
    this.sourceTypeVersion = sourceTypeVersion;
  }

  private static class NullSource<T> extends Source<T> {
    @Override
    public boolean isNull() {
      return true;
    }
  }
}
