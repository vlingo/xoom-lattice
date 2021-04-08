// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.sourcing;

import io.vlingo.xoom.common.serialization.JsonSerialization;
import io.vlingo.xoom.symbio.BaseEntry.TextEntry;
import io.vlingo.xoom.symbio.EntryAdapter;
import io.vlingo.xoom.symbio.Metadata;

public final class Test1HappenedAdapter implements EntryAdapter<Test1Happened, TextEntry> {
  @Override
  public Test1Happened fromEntry(final TextEntry entry) {
    return JsonSerialization.deserialized(entry.entryData(), Test1Happened.class);
  }

  @Override
  public TextEntry toEntry(final Test1Happened source, final Metadata metadata) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(Test1Happened.class, 1, serialization, metadata);
  }

  @Override
  public TextEntry toEntry(final Test1Happened source, final String id, final Metadata metadata) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(id, Test1Happened.class, 1, serialization, metadata);
  }

  @Override
  public TextEntry toEntry(Test1Happened source, int version, String id, Metadata metadata) {
    final String serialization = JsonSerialization.serialized(source);
    return new TextEntry(id, Test1Happened.class, 1, serialization, version, metadata);
  }
}
