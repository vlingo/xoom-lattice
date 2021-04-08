// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

import io.vlingo.xoom.common.serialization.JsonSerialization;
import io.vlingo.xoom.symbio.BaseEntry.TextEntry;
import io.vlingo.xoom.symbio.EntryAdapter;
import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.Source;

public class ProcessMessageTextAdapter implements EntryAdapter<ProcessMessage,TextEntry> {
  @Override
  public ProcessMessage fromEntry(final TextEntry entry) {
    try {
      final SerializableProcessMessage serializedMessage = JsonSerialization.deserialized(entry.entryData(), SerializableProcessMessage.class);
      final Class<?> sourceType = Class.forName(serializedMessage.type);
      final Source<?> source = (Source<?>) JsonSerialization.deserialized(serializedMessage.source, sourceType);
    return new ProcessMessage(source);
    } catch (Exception e) {
      throw new IllegalArgumentException("ProcessMessageTextAdapter failed because: " + e.getMessage(), e);
    }
  }

  @Override
  public TextEntry toEntry(final ProcessMessage source, final Metadata metadata) {
    final SerializableProcessMessage serializedMessage = new SerializableProcessMessage(source);
    final String serialization = JsonSerialization.serialized(serializedMessage);
    return new TextEntry(ProcessMessage.class, 1, serialization, metadata);
  }

  @Override
  public TextEntry toEntry(final ProcessMessage source, final String id, final Metadata metadata) {
    final SerializableProcessMessage serializedMessage = new SerializableProcessMessage(source);
    final String serialization = JsonSerialization.serialized(serializedMessage);
    return new TextEntry(id, ProcessMessage.class, 1, serialization, metadata);
  }

  @Override
  public TextEntry toEntry(final ProcessMessage source, final int version, final String id, final Metadata metadata) {
    final SerializableProcessMessage serializedMessage = new SerializableProcessMessage(source);
    final String serialization = JsonSerialization.serialized(serializedMessage);
    return new TextEntry(id, ProcessMessage.class, 1, serialization, version, metadata);
  }

  private static final class SerializableProcessMessage {
    public final String source;
    public final String type;

    SerializableProcessMessage(final ProcessMessage message) {
      this.source = sourceToText(message.source);
      this.type = message.sourceTypeName();
    }

    private String sourceToText(final Source<?> source) {
      final String sourceJson = JsonSerialization.serialized(source);
      return sourceJson;
    }
  }
}
