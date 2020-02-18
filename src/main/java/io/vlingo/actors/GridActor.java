package io.vlingo.actors;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class GridActor<T extends Serializable>
    extends Actor
    implements RelocationSnapshotSupplier<T>,
      RelocationSnapshotConsumer<T> {

  public static final String Resume = "GridActor.Resume";

  protected final void suspend() {
    lifeCycle.environment.mailbox.suspendExceptFor(Resume, RelocationSnapshotConsumer.class);
  }

  protected final boolean isSuspended() {
    return lifeCycle.environment.mailbox.isSuspendedFor(Resume);
  }

  protected final void resume() {
    lifeCycle.environment.mailbox.resume(Resume);
  }

  public final List<Message> pending() {
    final Mailbox mailbox = lifeCycle.environment.mailbox;
    return StreamSupport.stream(
        Spliterators.spliterator(new PendingMessageIterator(mailbox), mailbox.pendingMessages(), Spliterator.ORDERED),
        false)
      .collect(Collectors.toCollection(ArrayList::new));
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
