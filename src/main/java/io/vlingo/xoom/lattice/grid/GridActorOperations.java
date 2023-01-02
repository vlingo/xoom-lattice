// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Message;
import io.vlingo.xoom.actors.Relocatable;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.__InternalOnlyAccessor;

public final class GridActorOperations {
  static final String Resume = "GridActor.Resume";

  public static final <S> void applyRelocationSnapshot(final Stage stage, final Actor actor, final S snapshot) {
    final Relocatable consumer = stage.actorAs(actor, Relocatable.class);

    consumer.stateSnapshot(snapshot);
  }

  public static final <S> S supplyRelocationSnapshot(final Actor actor) {
    return actor.stateSnapshot();
  }

  public static final List<Message> pending(final Actor actor) {
    final Mailbox mailbox = __InternalOnlyAccessor.actorMailbox(actor);
    return StreamSupport.stream(
        Spliterators.spliterator(new PendingMessageIterator(mailbox), mailbox.pendingMessages(), Spliterator.ORDERED),
        false)
      .collect(Collectors.toCollection(ArrayList::new));
  }

  public static final void resumeFromRelocation(final Actor actor) {
    __InternalOnlyAccessor.actorMailbox(actor).resume(Resume);
  }

  public static final boolean isSuspendedForRelocation(final Actor actor) {
    return __InternalOnlyAccessor.actorMailbox(actor).isSuspendedFor(Resume);
  }

  public static final void suspendForRelocation(final Actor actor) {
    __InternalOnlyAccessor.actorMailbox(actor).suspendExceptFor(Resume, Relocatable.class);
  }

  private static class PendingMessageIterator implements Iterator<Message> {

    private final Mailbox mailbox;

    private Message next = null;

    PendingMessageIterator(Mailbox mailbox) {
      this.mailbox = mailbox;
    }

    @Override
    public boolean hasNext() {
      if (this.next == null) {
        this.next = mailbox.receive();
      }
      return this.next != null;
    }

    @Override
    public Message next() {
      if (hasNext()) {
        Message __next = this.next;
        this.next = null;
        return __next;
      }
      else {
        throw new NoSuchElementException();
      }
    }
  }
}
