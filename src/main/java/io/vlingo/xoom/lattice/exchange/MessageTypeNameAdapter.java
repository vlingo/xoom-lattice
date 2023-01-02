// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts internal message type names to a public type name. If given
 * a {@code startsWithReplacements} constructor parameter, uses the
 * {@code key} to match the internal type name prefix and replace it
 * with the {@code value}.
 */ 
public class MessageTypeNameAdapter {
  private final Map<String, String> startsWithReplacements;

  public MessageTypeNameAdapter(Map<String, String> startsWithReplacements) {
    this.startsWithReplacements = startsWithReplacements == null ? new HashMap<>(0) : startsWithReplacements;
  }

  /**
   * Answer the public type name from the internal type name using
   * my {@code startsWithReplacements} map.
   * @param internalTypeName the String name of the internal type
   * @return String
   */
  public String toPublicTypeName(final String internalTypeName) {
    for (final String prefix : startsWithReplacements.keySet()) {
      if (internalTypeName.startsWith(prefix)) {
        final String replacement = startsWithReplacements.get(prefix);
        final String postFix = internalTypeName.substring(prefix.length());
        final String publicTypeName = replacement + postFix;
        return publicTypeName;
      }
    }
    return internalTypeName;
  }
}
