// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import java.util.function.BiConsumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.AddressFactory;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.common.Completes;
import io.vlingo.lattice.grid.Grid;
import io.vlingo.lattice.model.Command;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RoutableCommand<P,A> extends Command {
  private Class<? extends Actor> actorType;
  private String address;
  private Completes<A> answer;
  private BiConsumer<P,Completes<A>> handler;
  private String id;
  private Class<P> protocol;

  public static <P,A> RoutableCommand<P,A> speaks(final Class<P> protocol) {
    return new RoutableCommand<>(protocol);
  }

  public RoutableCommand<P,A> to(final Class<? extends Actor> actorType) {
    this.actorType = actorType;
    return this;
  }

  public RoutableCommand<P,A> at(final String address) {
    this.address = address;
    return this;
  }

  public RoutableCommand<P,A> identity(final String id) {
    this.id = id;
    return this;
  }

  @Override
  public String id() {
    return (id == null || id.isEmpty() ? super.id() : id);
  }

  public <PROTOCOL,ANSWER> RoutableCommand<PROTOCOL,ANSWER> answers(final Completes<ANSWER> answer) {
    this.answer = (Completes<A>) answer;
    return (RoutableCommand<PROTOCOL,ANSWER>) this;
  }

  public <PROTOCOL,ANSWER> RoutableCommand<PROTOCOL,ANSWER> handledBy(final BiConsumer handler) {
    this.handler = handler;
    return (RoutableCommand<PROTOCOL,ANSWER>) this;
  }

  public void handleWithin(final Grid grid) {
    final AddressFactory addressFactory = grid.addressFactory;
    grid.actorOf(protocol, addressFactory.from(address))
        .otherwise(noActor -> grid.actorFor(protocol, Definition.has(actorType, Definition.NoParameters), addressFactory.from(address)))
        .andThenConsume(actor -> handler.accept(actor, answer));
  }

  public void handleWithin(final Stage stage) {
    final AddressFactory addressFactory = stage.world().addressFactory();
    stage.actorOf(protocol, addressFactory.from(address))
         .otherwise(noActor -> stage.actorFor(protocol, Definition.has(actorType, Definition.NoParameters), addressFactory.from(address)))
         .andThenConsume(actor -> handler.accept(actor, answer));
  }

  private RoutableCommand(final Class<P> protocol) {
    this.protocol = protocol;
    this.id = "";
  }
}
