// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.StateAdapter;

public class Entity1 {
  public final String id;
  public final int value;

  public Entity1(final String id, final int value) {
    this.id = id;
    this.value = value;
  }

  @Override
  public int hashCode() {
    return 31 * id.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }
    return this.id.equals(((Entity1) other).id);
  }

  @Override
  public String toString() {
    return "Entity1[id=" + id + " value=" + value + "]";
  }

  public static class Entity1StateAdapter implements StateAdapter<Entity1,TextState> {

    @Override
    public int typeVersion() {
      return 1;
    }

    @Override
    public Entity1 fromRawState(final TextState raw) {
      return JsonSerialization.deserialized(raw.data, raw.typed());
    }

    @Override
    public <ST> ST fromRawState(final TextState raw, final Class<ST> stateType) {
      return JsonSerialization.deserialized(raw.data, stateType);
    }

    @Override
    public TextState toRawState(final Entity1 state, final int stateVersion) {
      return toRawState(state, stateVersion, Metadata.with("value", "op"));
    }

    @Override
    public TextState toRawState(final Entity1 state, final int stateVersion, final Metadata metadata) {
      final String serialization = JsonSerialization.serialized(state);
      return new TextState(state.id, Entity1.class, typeVersion(), serialization, stateVersion, metadata);
    }
  }
}
