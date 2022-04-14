// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.Outcome;
import io.vlingo.xoom.lattice.model.TestEvents;
import io.vlingo.xoom.lattice.model.sourcing.MockJournalDispatcher;
import io.vlingo.xoom.symbio.BaseEntry.TextEntry;
import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.Source;
import io.vlingo.xoom.symbio.store.EntryReader;
import io.vlingo.xoom.symbio.store.Result;
import io.vlingo.xoom.symbio.store.StorageException;
import io.vlingo.xoom.symbio.store.journal.Journal;
import io.vlingo.xoom.symbio.store.journal.Journal.AppendResultInterest;
import io.vlingo.xoom.symbio.store.journal.inmemory.InMemoryJournalActor;

public class FeedTest {
  private MockFeedConsumer consumer;
  private MockJournalDispatcher dispatcher;
  private EntryReader<TextEntry> entryReader;
  private AppendResultInterest interest;
  private Journal<String> journal;
  private World world;

  @Test
  public void testThatDefaultFeedIsCreated() {
    final Feed feed = Feed.defaultFeedWith(world.stage(), "test", TextEntryReaderFeeder.class, entryReader);

    assertNotNull(feed);
  }

  @Test
  public void testThatDefaultFeedReadsFirstFeedItemIncomplete() {
    final Feed feed = Feed.defaultFeedWith(world.stage(), "test", TextEntryReaderFeeder.class, entryReader);

    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(3);

    journal.append("stream-1", 1, new TestEvents.Event1(), interest, null);
    journal.append("stream-2", 1, new TestEvents.Event2(), interest, null);
    journal.append("stream-3", 1, new TestEvents.Event3(), interest, null);

    final int count = dispatcherAccess.readFrom("entriesCount");

    assertEquals(3, count);

    final AccessSafely consumerAccess = consumer.afterCompleting(1);

    feed.feeder().feedItemTo(FeedItemId.with(1), consumer);

    final Map<Long,FeedItem> feedItems = consumerAccess.readFrom("feedItems");

    assertEquals(1, feedItems.size());
    assertFalse(feedItems.get(1L).archived);
  }

  @Test
  public void testThatDefaultFeedReadsFirstFeedItemArchived() {
    final Feed feed = Feed.defaultFeedWith(world.stage(), "test", TextEntryReaderFeeder.class, entryReader);

    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(feed.messagesPerFeedItem());

    for (int idx = 0; idx < feed.messagesPerFeedItem(); ++idx) {
      journal.append("stream-" + idx, 1, new TestEvents.Event1(), interest, null);
    }

    final int count = dispatcherAccess.readFrom("entriesCount");

    assertEquals(feed.messagesPerFeedItem(), count);

    final AccessSafely consumerAccess = consumer.afterCompleting(1);

    feed.feeder().feedItemTo(FeedItemId.with(1), consumer);

    final Map<Long,FeedItem> feedItems = consumerAccess.readFrom("feedItems");

    assertEquals(1, feedItems.size());
    assertEquals(feed.messagesPerFeedItem(), feedItems.get(1L).messages.size());
    assertTrue(feedItems.get(1L).archived);
  }

  @Test
  public void testThatDefaultFeedReadsFirstFeedItemArchivedSecondFeedItemIncomplete() {
    final Feed feed = Feed.defaultFeedWith(world.stage(), "test", TextEntryReaderFeeder.class, entryReader);

    final int extra = 3;
    final int entries = feed.messagesPerFeedItem() + extra;

    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(entries);

    for (int idx = 0; idx < entries; ++idx) {
      journal.append("stream-" + idx, 1, new TestEvents.Event1(), interest, null);
    }

    final int count = dispatcherAccess.readFrom("entriesCount");

    assertEquals(entries, count);

    final AccessSafely consumerAccess = consumer.afterCompleting(2);

    feed.feeder().feedItemTo(FeedItemId.with(1), consumer);
    feed.feeder().feedItemTo(FeedItemId.with(2), consumer);

    final Map<Long,FeedItem> feedItems = consumerAccess.readFrom("feedItems");

    assertEquals(2, feedItems.size());
    assertEquals(feed.messagesPerFeedItem(), feedItems.get(1L).messages.size());
    assertTrue(feedItems.get(1L).archived);
    assertEquals(extra, feedItems.get(2L).messages.size());
    assertFalse(feedItems.get(2L).archived);
  }

  @Test
  public void testThatDefaultFeedReadsThreeItems() {
    final Feed feed = Feed.defaultFeedWith(world.stage(), "test", TextEntryReaderFeeder.class, entryReader);

    final int extra = feed.messagesPerFeedItem() / 2;
    final int entries = feed.messagesPerFeedItem() * 2 + extra;

    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(entries);

    for (int idx = 0; idx < entries; ++idx) {
      journal.append("stream-" + idx, 1, new TestEvents.Event1(), interest, null);
    }

    final int count = dispatcherAccess.readFrom("entriesCount");

    assertEquals(entries, count);

    final AccessSafely consumerAccess = consumer.afterCompleting(3);

    feed.feeder().feedItemTo(FeedItemId.with(1), consumer);
    feed.feeder().feedItemTo(FeedItemId.with(2), consumer);
    feed.feeder().feedItemTo(FeedItemId.with(3), consumer);

    final Map<Long,FeedItem> feedItems = consumerAccess.readFrom("feedItems");

    assertEquals(3, feedItems.size());
    assertEquals(feed.messagesPerFeedItem(), feedItems.get(1L).messages.size());
    assertTrue(feedItems.get(1L).archived);
    assertEquals(feed.messagesPerFeedItem(), feedItems.get(2L).messages.size());
    assertTrue(feedItems.get(2L).archived);
    assertEquals(extra, feedItems.get(3L).messages.size());
    assertFalse(feedItems.get(3L).archived);
  }

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    world = World.startWithDefaults("feed-test");

    dispatcher = new MockJournalDispatcher();

    journal = world.actorFor(Journal.class, InMemoryJournalActor.class, Arrays.asList(dispatcher));

    entryReader = journal.journalReader("feed-test-reader").await();

    consumer = new MockFeedConsumer();

    interest = noOpInterest();
  }

  @After
  public void tearDown() {
    entryReader.close();
    world.terminate();
  }

  private AppendResultInterest noOpInterest() {
    return new AppendResultInterest() {
      @Override public <S, ST> void appendResultedIn(Outcome<StorageException, Result> outcome, String streamName, int streamVersion, Source<S> source, Optional<ST> snapshot, Object object) { }
      @Override public <S, ST> void appendResultedIn(Outcome<StorageException, Result> outcome, String streamName, int streamVersion, Source<S> source, Metadata metadata, Optional<ST> snapshot, Object object) { }
      @Override public <S, ST> void appendAllResultedIn(Outcome<StorageException, Result> outcome, String streamName, int streamVersion, List<Source<S>> sources, Optional<ST> snapshot, Object object) { }
      @Override public <S, ST> void appendAllResultedIn(Outcome<StorageException, Result> outcome, String streamName, int streamVersion, List<Source<S>> sources, Metadata metadata, Optional<ST> snapshot, Object object) { }
    };
  }
}
