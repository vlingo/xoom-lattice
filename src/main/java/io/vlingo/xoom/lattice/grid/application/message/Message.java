// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application.message;

import java.io.Serializable;

import io.vlingo.xoom.wire.node.Id;

public interface Message extends Serializable {
  void accept(Id receiver, Id sender, Visitor visitor);
}
