// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice;

import java.util.regex.Pattern;

/**
 * Composite identity support mix-in.
 */
public interface CompositeIdentitySupport {

  /**
   * Answer the String data id from the {@code idSegments} separated by {@code separator}.
   * @param separator the String used to separate the segments
   * @param idSegments the String[] of identities to compose
   * @return String
   */
  default String dataIdFrom(final String separator, final String... idSegments) {
    final StringBuilder builder = new StringBuilder();
    builder.append(idSegments[0]);
    for (int idx = 1; idx < idSegments.length; ++idx) {
      builder.append(separator).append(idSegments[idx]);
    }
    return builder.toString();
  }

  /**
   * Answer the {@code String[]} of segments from the {@code dataId} that are
   * separated by the {@code separator}.
   * @param separator the String that separates the segments
   * @param dataId the String composite identity
   * @return {@code String[]}
   */
  default String[] dataIdSegmentsFrom(final String separator, final String dataId) {
    return dataId.split(Pattern.quote(separator));
  }
}
