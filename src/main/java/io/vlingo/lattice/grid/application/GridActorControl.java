// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.application;

import java.util.List;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Returns;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.wire.node.Id;

public interface GridActorControl {

  <T> void start(Id recipient,
                 Id sender,
                 Class<T> protocol,
                 Address address,
                 Definition.SerializationProxy definitionProxy);

  <T> void deliver(Id recipient,
                   Id sender,
                   Returns<?> returns,
                   Class<T> protocol,
                   Address address,
                   Definition.SerializationProxy definitionProxy,
                   SerializableConsumer<T> consumer,
                   String representation);

  <T> void answer(Id receiver,
                  Id sender,
                  Answer<T> answer);

  void forward(Id receiver, Id sender, Message message);

  void relocate(Id receiver,
                Id sender,
                Definition.SerializationProxy definitionProxy,
                Address address,
                Object snapshot,
                List<? extends io.vlingo.actors.Message> pending);


  interface Inbound extends GridActorControl {
  }

  interface Outbound extends GridActorControl {
  }
}
