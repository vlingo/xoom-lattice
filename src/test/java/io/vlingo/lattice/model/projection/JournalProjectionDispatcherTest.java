// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Grid;
import io.vlingo.actors.GridActor;
import io.vlingo.actors.Protocols;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.Outcome;
import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.lattice.model.DomainEvent;
import io.vlingo.lattice.model.projection.ProjectionDispatcher.ProjectToDescription;
import io.vlingo.lattice.model.projection.ProjectionDispatcher.TextProjectionDispatcherInstantiator;
import io.vlingo.symbio.BaseEntry.TextEntry;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.EntryAdapter;
import io.vlingo.symbio.EntryAdapterProvider;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.dispatch.Dispatchable;
import io.vlingo.symbio.store.dispatch.Dispatcher;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.Journal.AppendResultInterest;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournalActor;

public class JournalProjectionDispatcherTest {
  private static final String AccessJournal = "journal";
  private static final String AccessProjection = "projection";
  private static final String StreamName = "A123";

  private AccessHolder accessHolder;
  private AppendResultInterest appendInterest;
  private Dispatcher<Dispatchable<Entry<String>,State<String>>> dispatcher;
  private Grid grid;
  private Journal<String> journal;

  @Test
  public void testThatOneTwoAllEventsProject() {
    accessHolder.accessJournalFor(1);
    accessHolder.accessProjectionFor(5); // One, Two, and All

    journal.appendAll(StreamName, 1, Arrays.asList(new OneHappened(), new TwoHappened(), new ThreeHappened()), appendInterest, accessHolder);

    assertEquals(1, (int) accessHolder.accessJournal.readFrom(AccessJournal));
    assertEquals(5, (int) accessHolder.accessProjection.readFrom(AccessProjection));
  }

  @Test
  public void testThatOneEventProject() {
    accessHolder.accessJournalFor(1);
    accessHolder.accessProjectionFor(2); // One and All

    journal.append(StreamName, 1, new OneHappened(), appendInterest, accessHolder);

    assertEquals(1, (int) accessHolder.accessJournal.readFrom(AccessJournal));
    assertEquals(2, (int) accessHolder.accessProjection.readFrom(AccessProjection));
  }

  @Test
  public void testThatTwoEventProject() {
    accessHolder.accessJournalFor(1);
    accessHolder.accessProjectionFor(2); // Two and All

    journal.append(StreamName, 1, new TwoHappened(), appendInterest, accessHolder);

    assertEquals(1, (int) accessHolder.accessJournal.readFrom(AccessJournal));
    assertEquals(2, (int) accessHolder.accessProjection.readFrom(AccessProjection));
  }

  @Test
  public void testThatThreeEventProject() {
    accessHolder.accessJournalFor(1);
    accessHolder.accessProjectionFor(1); // Only All

    journal.append(StreamName, 1, new ThreeHappened(), appendInterest, accessHolder);

    assertEquals(1, (int) accessHolder.accessJournal.readFrom(AccessJournal));
    assertEquals(1, (int) accessHolder.accessProjection.readFrom(AccessProjection));
  }

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() throws Exception {
    grid = Grid.startWith("test-journal-projections", "node1");

    accessHolder = new AccessHolder();

    final List<ProjectToDescription> descriptions =
            Arrays.asList(
                    ProjectToDescription.with(OneHappenedProjectionActor.class, Optional.of(accessHolder), OneHappened.class),
                    ProjectToDescription.with(TwoHappenedProjectionActor.class, Optional.of(accessHolder), TwoHappened.class),
                    ProjectToDescription.with(AllHappenedProjectionActor.class, Optional.of(accessHolder), OneHappened.class.getPackage()));

    final Protocols dispatcherProtocols =
            grid.actorFor(
                    new Class<?>[] { Dispatcher.class, ProjectionDispatcher.class },
                    Definition.has(TextProjectionDispatcherActor.class, new TextProjectionDispatcherInstantiator(descriptions)));

    final Protocols.Two<Dispatcher, ProjectionDispatcher> dispatchers = Protocols.two(dispatcherProtocols);

    this.dispatcher = dispatchers._1;

    journal = Journal.using(grid, InMemoryJournalActor.class, this.dispatcher);

    EntryAdapterProvider.instance(grid.world()).registerAdapter(OneHappened.class, new OneHappenedAdapter());
    EntryAdapterProvider.instance(grid.world()).registerAdapter(TwoHappened.class, new TwoHappenedAdapter());
    EntryAdapterProvider.instance(grid.world()).registerAdapter(ThreeHappened.class, new ThreeHappenedAdapter());

    appendInterest = grid.actorFor(AppendResultInterest.class, JournalAppendResultInterest.class);
  }


  private static class AccessHolder {
    private AccessSafely accessJournal;
    private AtomicInteger accessJournalCount = new AtomicInteger(0);
    private AccessSafely accessProjection;
    private AtomicInteger accessProjectionCount = new AtomicInteger(0);

    private AccessSafely accessJournalFor(final int times) {
      accessJournal = AccessSafely.afterCompleting(times);

      accessJournal.writingWith(AccessJournal, (x) -> accessJournalCount.incrementAndGet());
      accessJournal.readingWith(AccessJournal, () -> accessJournalCount.get());

      return accessJournal;
    }

    private AccessSafely accessProjectionFor(final int times) {
      accessProjection = AccessSafely.afterCompleting(times);

      accessProjection.writingWith(AccessProjection, (x) -> accessProjectionCount.incrementAndGet());
      accessProjection.readingWith(AccessProjection, () -> accessProjectionCount.get());

      return accessProjection;
    }
  }


  private static class OneHappened extends DomainEvent {
    OneHappened() { }
  }

  private static class TwoHappened extends DomainEvent {
    TwoHappened() { }
  }

  private static class ThreeHappened extends DomainEvent {
    ThreeHappened() { }
  }

  public static class OneHappenedProjectionActor extends GridActor<OneHappenedProjectionActor.Proxy> implements Projection {
    private final AccessHolder accessHolder;

    public OneHappenedProjectionActor(final AccessHolder accessHolder) {
      this.accessHolder = accessHolder;
    }

    @Override
    public void projectWith(final Projectable projectable, final ProjectionControl control) {
      projectable.entries().forEach(entry -> {
        switch (entry.typed().getSimpleName()) {
        case "OneHappened":
          accessHolder.accessProjection.writeUsing(AccessProjection, 1);
          ProjectionControl.confirmerFor(projectable, control).confirm();
          logger().debug("ONE");
          break;
        default:
          break;
        }
      });
    }

    public static final class Proxy implements Serializable {
      private static final long serialVersionUID = -2796142731077588067L;

      public Proxy() {
      }

      @Override
      public String toString() {
        return String.format("Proxy(none)");
      }
    }

    @Override
    public Proxy provideRelocationSnapshot() {
      return new Proxy();
    }

    @Override
    public void applyRelocationSnapshot(Proxy snapshot) {
    }
  }

  public static class TwoHappenedProjectionActor extends GridActor<TwoHappenedProjectionActor.Proxy> implements Projection {
    private final AccessHolder accessHolder;

    public TwoHappenedProjectionActor(final AccessHolder accessHolder) {
      this.accessHolder = accessHolder;
    }

    @Override
    public void projectWith(final Projectable projectable, final ProjectionControl control) {
      projectable.entries().forEach(entry -> {
        switch (entry.typed().getSimpleName()) {
        case "TwoHappened":
          accessHolder.accessProjection.writeUsing(AccessProjection, 1);
          ProjectionControl.confirmerFor(projectable, control).confirm();
          logger().debug("TWO");
          break;
        default:
          break;
        }
      });
    }

    public static final class Proxy implements Serializable {
      private static final long serialVersionUID = -2796142731077588067L;

      public Proxy() {
      }

      @Override
      public String toString() {
        return String.format("Proxy(none)");
      }
    }

    @Override
    public Proxy provideRelocationSnapshot() {
      return new Proxy();
    }

    @Override
    public void applyRelocationSnapshot(Proxy snapshot) {
    }
  }

  public static class AllHappenedProjectionActor extends GridActor<AllHappenedProjectionActor.Proxy> implements Projection {
    private final AccessHolder accessHolder;
    private int count;

    public AllHappenedProjectionActor(final AccessHolder accessHolder) {
      this.accessHolder = accessHolder;
    }

    @Override
    public void projectWith(final Projectable projectable, final ProjectionControl control) {
      count = 0;
      projectable.entries().forEach(entry -> {
        switch (entry.typed().getSimpleName()) {
        case "OneHappened":
        case "TwoHappened":
        case "ThreeHappened":
          accessHolder.accessProjection.writeUsing(AccessProjection, 1);
          logger().debug("ALL " + (++count));
          break;
        default:
          break;
        }
      });
      ProjectionControl.confirmerFor(projectable, control).confirm();
    }

    public static final class Proxy implements Serializable {
      private static final long serialVersionUID = -2796142731077588067L;

      final int count;

      public Proxy(int count) {
        this.count = count;
      }

      @Override
      public String toString() {
        return String.format("Proxy(count='%d')", count);
      }
    }

    @Override
    public Proxy provideRelocationSnapshot() {
      return new Proxy(count);
    }

    @Override
    public void applyRelocationSnapshot(Proxy snapshot) {
      this.count = snapshot.count;
    }
  }

  public static class JournalAppendResultInterest extends Actor implements AppendResultInterest {
    public JournalAppendResultInterest() { }

    @Override
    public <S, ST> void appendResultedIn(Outcome<StorageException, Result> outcome, String streamName,
            int streamVersion, Source<S> source, Optional<ST> snapshot, Object object) {
      logger().debug("APPENDED");
      ((AccessHolder) object).accessJournal.writeUsing(AccessJournal, 1);
    }

    @Override
    public <S, ST> void appendResultedIn(Outcome<StorageException, Result> outcome, String streamName,
            int streamVersion, Source<S> source, Metadata metadata, Optional<ST> snapshot, Object object) {
      logger().debug("APPENDED");
      ((AccessHolder) object).accessJournal.writeUsing(AccessJournal, 1);
    }

    @Override
    public <S, ST> void appendAllResultedIn(Outcome<StorageException, Result> outcome, String streamName,
            int streamVersion, List<Source<S>> sources, Optional<ST> snapshot, Object object) {
      logger().debug("APPENDED");
      ((AccessHolder) object).accessJournal.writeUsing(AccessJournal, 1);
    }

    @Override
    public <S, ST> void appendAllResultedIn(Outcome<StorageException, Result> outcome, String streamName,
            int streamVersion, List<Source<S>> sources, Metadata metadata, Optional<ST> snapshot, Object object) {
      logger().debug("APPENDED");
      ((AccessHolder) object).accessJournal.writeUsing(AccessJournal, 1);
    }
  }

  public static final class OneHappenedAdapter implements EntryAdapter<OneHappened, TextEntry> {
    @Override
    public OneHappened fromEntry(final TextEntry entry) {
      return JsonSerialization.deserialized(entry.entryData(), OneHappened.class);
    }

    @Override
    public TextEntry toEntry(OneHappened source, Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(OneHappened.class, 1, serialization, metadata);
    }

    @Override
    public TextEntry toEntry(OneHappened source, String id, Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(id, OneHappened.class, 1, serialization, metadata);
    }

    @Override
    public TextEntry toEntry(final OneHappened source, final int version, final String id, final Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(id, OneHappened.class, 1, serialization, version, metadata);
    }
  }

  public static final class TwoHappenedAdapter implements EntryAdapter<TwoHappened, TextEntry> {
    @Override
    public TwoHappened fromEntry(final TextEntry entry) {
      return JsonSerialization.deserialized(entry.entryData(), TwoHappened.class);
    }

    @Override
    public TextEntry toEntry(TwoHappened source, Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(TwoHappened.class, 1, serialization, metadata);
    }

    @Override
    public TextEntry toEntry(TwoHappened source, String id, Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(id, TwoHappened.class, 1, serialization, metadata);
    }

    @Override
    public TextEntry toEntry(final TwoHappened source, final int version, final String id, final Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(id, TwoHappened.class, 1, serialization, version, metadata);
    }
  }

  public static final class ThreeHappenedAdapter implements EntryAdapter<ThreeHappened, TextEntry> {
    @Override
    public ThreeHappened fromEntry(final TextEntry entry) {
      return JsonSerialization.deserialized(entry.entryData(), ThreeHappened.class);
    }

    @Override
    public TextEntry toEntry(ThreeHappened source, Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(ThreeHappened.class, 1, serialization, metadata);
    }

    @Override
    public TextEntry toEntry(ThreeHappened source, String id, Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(id, ThreeHappened.class, 1, serialization, metadata);
    }

    @Override
    public TextEntry toEntry(final ThreeHappened source, final int version, final String id, final Metadata metadata) {
      final String serialization = JsonSerialization.serialized(source);
      return new TextEntry(id, ThreeHappened.class, 1, serialization, version, metadata);
    }
  }
}
