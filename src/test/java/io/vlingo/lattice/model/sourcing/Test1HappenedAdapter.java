// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.symbio.EntryAdapter;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.BaseEntry.TextEntry;

public final class Test1HappenedAdapter implements EntryAdapter<Test1Happened,TextEntry> {
  @Override
  public Test1Happened fromEntry(final TextEntry entry) {
    return JsonSerialization.deserialized(entry.entryData(), Test1Happened.class);
  }

  @Override
  public TextEntry toEntry(final Test1Happened source) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(Test1Happened.class, 1, serialization, Metadata.nullMetadata());
  }

  @Override
  public TextEntry toEntry(final Test1Happened source, final String id) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(id, Test1Happened.class, 1, serialization, Metadata.nullMetadata());
  }
}
