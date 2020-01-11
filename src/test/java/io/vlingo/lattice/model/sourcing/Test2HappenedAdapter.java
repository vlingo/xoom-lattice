// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.symbio.BaseEntry.TextEntry;
import io.vlingo.symbio.EntryAdapter;
import io.vlingo.symbio.Metadata;

public final class Test2HappenedAdapter implements EntryAdapter<Test2Happened, TextEntry> {
  @Override
  public Test2Happened fromEntry(final TextEntry entry) {
    return JsonSerialization.deserialized(entry.entryData(), Test2Happened.class);
  }

  @Override
  public TextEntry toEntry(final Test2Happened source, final Metadata metadata) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(Test2Happened.class, 1, serialization, metadata);
  }

  @Override
  public TextEntry toEntry(final Test2Happened source, final String id, final Metadata metadata) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(id, Test2Happened.class, 1, serialization, metadata);
  }

  @Override
  public TextEntry toEntry(Test2Happened source, int version, String id, Metadata metadata) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(id, Test2Happened.class, 1, serialization, version, metadata);
  }
}
