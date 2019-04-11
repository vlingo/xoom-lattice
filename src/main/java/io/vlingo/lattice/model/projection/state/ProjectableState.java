// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import java.util.Collection;

import io.vlingo.lattice.model.projection.Projectable;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;

public abstract class ProjectableState implements Projectable {
  private final Collection<Entry<?>> entries;
  private final String projectionId;
  private final State<?> state;

  public ProjectableState(final State<?> state, final Collection<Entry<?>> entries, final String projectionId) {
    this.state = state;
    this.entries = entries;
    this.projectionId = projectionId;
  }

  @Override
  public String becauseOf() {
    return state.metadata.operation;
  }

  @Override
  public byte[] dataAsBytes() {
    throw new UnsupportedOperationException("Projectable data is not binary compatible.");
  }

  @Override
  public String dataAsText() {
    throw new UnsupportedOperationException("Projectable data is not text compatible.");
  }

  @Override
  public int dataVersion() {
    return state.dataVersion;
  }

  @Override
  public String dataId() {
    return state.id;
  }

  @Override
  public Collection<Entry<?>> entries() {
    return entries;
  }

  @Override
  public String metadata() {
    return state.metadata.value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T object() {
    return (T) state.metadata.object;
  }

  @Override
  public String projectionId() {
    return projectionId;
  }

  @Override
  public String type() {
    return state.type;
  }

  @Override
  public int typeVersion() {
    return state.typeVersion;
  }

  @SuppressWarnings("unchecked")
  protected State<byte[]> binaryState() {
    return (State<byte[]>) this.state;
  }

  @SuppressWarnings("unchecked")
  protected State<String> textState() {
    return (State<String>) this.state;
  }
}
