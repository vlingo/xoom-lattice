// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.router;

import java.time.Duration;
import java.util.List;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.Command;

/**
 * A {@code Command} that may be routed through a defined {@code CommandRouter}.
 * @param <P> the protocol type
 * @param <C> the command type
 * @param <A> the answer (outcome) type
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class RoutableCommand<P,C extends Command,A> extends Command {
  private Class<? extends Actor> actorType;
  private String address;
  private String name = "";
  private Completes<A> answer;
  private C command;
  private List<Object> creationParameters;
  private CommandDispatcher<P,C,Completes<A>> handler;
  private Class<P> protocol;
  private long timeout = -1L;

  /**
   * Answer a new {@code RoutableCommand} that speaks the {@code protocol}.
   * @param protocol the Class<P> class type of protocol
   * @return {@code RoutableCommand<P,C,A>}
   * @param <P> the protocol type
   * @param <C> the command type
   * @param <A> the answer/outcome type
   */
  public static <P,C extends Command,A> RoutableCommand<P,C,A> speaks(final Class<P> protocol) {
    assert(protocol != null);
    return new RoutableCommand<>(protocol);
  }

  /**
   * Answer myself after assigning my {@code actorType}.
   * @param actorType the {@code Class<? extends Actor>} of the actor
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> to(final Class<? extends Actor> actorType) {
    assert(actorType != null);
    this.actorType = actorType;
    return this;
  }

  /**
   * Answer myself after assigning my {@code address}.
   * @param address the long address
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> at(final long address) {
    assert(address != -1L);
    this.address = String.valueOf(address);
    return this;
  }

  /**
   * Answer myself after assigning my {@code address}.
   * @param address the String address
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> at(final String address) {
    assert(address != null);
    this.address = address;
    return this;
  }

  /**
   * Answer myself after assigning my {@code creationParameters}.
   * @param creationParameters the {@code List<Object>} address
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> createsWith(final List<Object> creationParameters) {
    assert(creationParameters != null);
    this.creationParameters = creationParameters;
    return this;
  }

  /**
   * Answer myself after assigning my actor {@code name}.
   * @param name the String name of the actor
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> named(final String name) {
    assert(name != null);
    this.name = name;
    return this;
  }

  /**
   * Answer myself after assigning my {@code command}.
   * @param command the C typed command
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> delivers(final C command) {
    assert(command != null);
    this.command = command;
    return this;
  }

  /**
   * Answer myself after assigning my {@code timeout}.
   * @param timeout the long timeout
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> timeout(final long timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Answer myself after assigning my {@code timeout}.
   * @param timeout the Duration timeout
   * @return {@code RoutableCommand<P,C,A>}
   */
  public RoutableCommand<P,C,A> timeout(final Duration timeout) {
    return timeout(timeout.toMillis());
  }

  /**
   * Answer my {@code command}.
   * @return C
   */
  public C command() {
    return command;
  }

  /**
   * Answer a new copy of myself.
   * @return RoutableCommand
   */
  public RoutableCommand copy() {
    return new RoutableCommand(protocol, actorType, address, command, answer, handler, creationParameters);
  }

  /**
   * Answer my {@code id}.
   * @return String
   */
  @Override
  public String id() {
    return (command == null || command.id().isEmpty() ? super.id() : command.id());
  }

  /**
   * Answer myself after setting my {@code answer}.
   * @param answer the ANSWER that serves as my means to answer
   * @return RoutableCommand
   * @param <PROTOCOL> the protocol type
   * @param <COMMAND> the command type
   * @param <ANSWER> the answer/outcome type
   */
  public <PROTOCOL,COMMAND extends Command,ANSWER> RoutableCommand<PROTOCOL,COMMAND,ANSWER> answers(final Completes<ANSWER> answer) {
    assert(answer != null);
    this.answer = (Completes<A>) answer;
    return (RoutableCommand<PROTOCOL,COMMAND,ANSWER>) this;
  }

  /**
   * Answer my {@code answer}.
   * @return {@code Completes<A>}
   */
  public Completes<A> answer() {
    return answer;
  }

  /**
   * Answer myself after assigning my {@code handler}.
   * @param handler the CommandDispatcher to assign as my handler
   * @return {@code RoutableCommand<PROTOCOL,COMMAND,ANSWER>}
   * @param <PROTOCOL> the protocol type
   * @param <COMMAND> the command type
   * @param <ANSWER> the answer/outcome type
   */
  public <PROTOCOL,COMMAND extends Command,ANSWER> RoutableCommand<PROTOCOL,COMMAND,ANSWER> handledBy(final CommandDispatcher handler) {
    assert(handler != null);
    this.handler = handler;
    return (RoutableCommand<PROTOCOL,COMMAND,ANSWER>) this;
  }

  /**
   * Handle my command my means of {@code stage}.
   * @param stage the Stage to use for command handling
   */
  public void handleWithin(final Stage stage) {
    check();

    final Address actorAddress = stage.addressFactory().from(address, name);

    stage.actorOf(protocol, actorAddress)
         .andThenConsume(timeout, actor -> {
           handler.accept(actor, command, answer);
          })
         .otherwise(noActor -> {
           final P actor = stage.actorFor(protocol, Definition.has(actorType, creationParameters, name), actorAddress);
           handler.accept(actor, command, answer);
           return actor;
         });
  }

  protected RoutableCommand(final Class<P> protocol) {
    this.protocol = protocol;
    this.creationParameters = Definition.NoParameters;
  }

  protected RoutableCommand(
          final Class<P> protocol,
          final Class<? extends Actor> actorType,
          final String address,
          final C command,
          final Completes<A> answer,
          final CommandDispatcher handler,
          final List<Object> creationParameters) {
    this.protocol = protocol;
    this.actorType = actorType;
    this.address = address;
    this.command = command;
    this.answer = answer;
    this.handler = handler;
    this.creationParameters = creationParameters;
  }

  private void check() {
    assert(protocol != null);
    assert(actorType != null);
    assert(address != null);
    assert(command != null);
    assert(handler != null);
  }
}
