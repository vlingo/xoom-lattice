// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application;

import io.vlingo.xoom.wire.message.RawMessage;
import io.vlingo.xoom.wire.node.Id;

public interface ApplicationMessageHandler {

  void handle(final RawMessage message);
}
