// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import io.vlingo.symbio.State;

public class ProjectableBinaryState extends ProjectableState {
  public ProjectableBinaryState(final State<byte[]> state, final String projectionId) {
    super(state, projectionId);
  }

  @Override
  public byte[] dataAsBytes() {
    return binaryState().data;
  }
}
