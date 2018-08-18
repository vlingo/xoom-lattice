// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.stateful;

import io.vlingo.actors.Actor;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.BinaryState;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.Result;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

public abstract class StatefulEntity<S,R> extends Actor
    implements Stateful<S>, ReadResultInterest<R>, WriteResultInterest<R> {

  @Override
  @SuppressWarnings("unchecked")
  public void preserve(final S state) {
    final Info<S,R> info = StatefulTypeRegistry.instance.info(stateType());
    final R raw = info.adapter.to(state, stateVersion(), typeVersion());
    if (info.isBinary()) {
      stowMessages(WriteResultInterest.class);
      info.binaryStateStore().write(new BinaryState(id(), (Class<S>) stateType(), typeVersion(), (byte[]) raw, stateVersion() + 1), selfAs(WriteResultInterest.class));
    } else {
      stowMessages(WriteResultInterest.class);
      info.textStateStore().write(new TextState(id(), (Class<S>) stateType(), typeVersion(), (String) raw, stateVersion() + 1), selfAs(WriteResultInterest.class));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void restore() {
    final Info<S,R> info = StatefulTypeRegistry.instance.info(stateType());
    if (info.isBinary()) {
      stowMessages(ReadResultInterest.class);
      info.binaryStateStore().read(id(), (Class<S>) stateType(), selfAs(ReadResultInterest.class));
    } else {
      stowMessages(ReadResultInterest.class);
      info.textStateStore().read(id(), (Class<S>) stateType(), selfAs(ReadResultInterest.class));
    }
  }

  @Override
  public void readResultedIn(final Result result, final String id, final State<R> state) {
    final Info<S,R> info = StatefulTypeRegistry.instance.info(stateType());
    final S preserved = info.adapter.from(state.data, stateVersion(), typeVersion());
    state(preserved, state.dataVersion);
    disperseStowedMessages();
  }

  @Override
  public void readResultedIn(final Result result, final Exception cause, final String id, final State<R> state) {
    final String message = "State not restored because: " + result + " with: " + cause.getMessage();
    logger().log(message, cause);
    throw new IllegalStateException(message, cause);
  }

  @Override
  public void writeResultedIn(final Result result, final String id, final State<R> state) {
    final Info<S,R> info = StatefulTypeRegistry.instance.info(stateType());
    final S preserved = info.adapter.from(state.data, stateVersion(), typeVersion());
    state(preserved, state.dataVersion);
    disperseStowedMessages();
  }

  @Override
  public void writeResultedIn(final Result result, final Exception cause, final String id, final State<R> state) {
    final String message = "State not preserved because: " + result + " with: " + cause.getMessage();
    logger().log(message, cause);
    throw new IllegalStateException(message, cause);
  }

  protected StatefulEntity() {
  }
}
