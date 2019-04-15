// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import io.vlingo.actors.Actor;
import io.vlingo.actors.AddressFactory;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.common.Completes;
import io.vlingo.lattice.grid.Grid;
import io.vlingo.lattice.model.Command;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RoutableCommand<P,C extends Command,A> extends Command {
  private Class<? extends Actor> actorType;
  private String address;
  private Completes<A> answer;
  private C command;
  private CommandDispatcher<P,C,Completes<A>> handler;
  private Class<P> protocol;

  public static <P,C extends Command,A> RoutableCommand<P,C,A> speaks(final Class<P> protocol) {
    assert(protocol != null);
    return new RoutableCommand<>(protocol);
  }

  public RoutableCommand<P,C,A> to(final Class<? extends Actor> actorType) {
    assert(actorType != null);
    this.actorType = actorType;
    return this;
  }

  public RoutableCommand<P,C,A> at(final String address) {
    assert(address != null);
    this.address = address;
    return this;
  }

  public RoutableCommand<P,C,A> delivers(final C command) {
    assert(command != null);
    this.command = command;
    return this;
  }

  public C command() {
    return command;
  }

  @Override
  public String id() {
    return (command == null || command.id().isEmpty() ? super.id() : command.id());
  }

  public <PROTOCOL,COMMAND extends Command,ANSWER> RoutableCommand<PROTOCOL,COMMAND,ANSWER> answers(final Completes<ANSWER> answer) {
    assert(answer != null);
    this.answer = (Completes<A>) answer;
    return (RoutableCommand<PROTOCOL,COMMAND,ANSWER>) this;
  }

  public Completes<A> answer() {
    return answer;
  }

  public <PROTOCOL,COMMAND extends Command,ANSWER> RoutableCommand<PROTOCOL,COMMAND,ANSWER> handledBy(final CommandDispatcher handler) {
    assert(handler != null);
    this.handler = handler;
    return (RoutableCommand<PROTOCOL,COMMAND,ANSWER>) this;
  }

  public void handleWithin(final Grid grid) {
    check();
    final AddressFactory addressFactory = grid.addressFactory;
    grid.actorOf(protocol, addressFactory.from(address))
        .otherwise(noActor -> grid.actorFor(protocol, Definition.has(actorType, Definition.NoParameters), addressFactory.from(address)))
        .andThenConsume(actor -> handler.accept(actor, command, answer));
  }

  public void handleWithin(final Stage stage) {
    check();
    final AddressFactory addressFactory = stage.world().addressFactory();
    stage.actorOf(protocol, addressFactory.from(address))
         .otherwise(noActor -> stage.actorFor(protocol, Definition.has(actorType, Definition.NoParameters), addressFactory.from(address)))
         .andThenConsume(actor -> handler.accept(actor, command, answer));
  }

  protected RoutableCommand(final Class<P> protocol) {
    this.protocol = protocol;
  }

  private void check() {
    assert(protocol != null);
    assert(actorType != null);
    assert(address != null);
    assert(command != null);
    assert(handler != null);
  }
}
