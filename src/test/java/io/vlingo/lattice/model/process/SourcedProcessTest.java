// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.common.message.AsyncMessageQueue;
import io.vlingo.common.message.MessageQueue;
import io.vlingo.lattice.exchange.Covey;
import io.vlingo.lattice.exchange.Exchange;
import io.vlingo.lattice.exchange.local.LocalExchange;
import io.vlingo.lattice.exchange.local.LocalExchangeAdapter;
import io.vlingo.lattice.exchange.local.LocalExchangeMessage;
import io.vlingo.lattice.exchange.local.LocalExchangeSender;
import io.vlingo.lattice.model.process.EntryAdapters.DoStepFiveAdapter;
import io.vlingo.lattice.model.process.EntryAdapters.DoStepFourAdapter;
import io.vlingo.lattice.model.process.EntryAdapters.DoStepThreeAdapter;
import io.vlingo.lattice.model.process.EntryAdapters.DoStepTwoAdapter;
import io.vlingo.lattice.model.process.EntryAdapters.MarkCompletedAdapter;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepFive;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepFour;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepThree;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepTwo;
import io.vlingo.lattice.model.process.FiveStepProcess.MarkCompleted;
import io.vlingo.lattice.model.process.ProcessTypeRegistry.SourcedProcessInfo;
import io.vlingo.lattice.model.sourcing.MockJournalListener;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournal;

public class SourcedProcessTest {
  private Exchange exchange;
  private ExchangeReceivers exchangeReceivers;
  private Journal<String> journal;
  private MockJournalListener listener;
  private FiveStepProcess process;
  private ProcessTypeRegistry processTypeRegistry;
  private SourcedTypeRegistry sourcedTypeRegistry;
  private World world;

  @Test
  public void testFiveStepSourcedProcess() {
    process.stepOneHappened();

    assertEquals(5, (int) exchangeReceivers.access.readFrom("stepCount"));

    assertEquals(5, (int) process.queryStepCount().await());
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    world = World.startWithDefaults("five-step-process-test");

    listener = new MockJournalListener();

    journal = new InMemoryJournal<>(listener);

    sourcedTypeRegistry = new SourcedTypeRegistry(world);

    sourcedTypeRegistry.register(new Info(journal, FiveStepSourcedProcess.class, FiveStepSourcedProcess.class.getSimpleName()));

    sourcedTypeRegistry.info(FiveStepSourcedProcess.class)
      .registerEntryAdapter(DoStepTwo.class, new DoStepTwoAdapter(),
              (type, adapter) -> journal.registerEntryAdapter(type, adapter))
      .registerEntryAdapter(DoStepThree.class, new DoStepThreeAdapter(),
              (type, adapter) -> journal.registerEntryAdapter(type, adapter))
      .registerEntryAdapter(DoStepFour.class, new DoStepFourAdapter(),
              (type, adapter) -> journal.registerEntryAdapter(type, adapter))
      .registerEntryAdapter(DoStepFive.class, new DoStepFiveAdapter(),
            (type, adapter) -> journal.registerEntryAdapter(type, adapter))
      .registerEntryAdapter(MarkCompleted.class, new MarkCompletedAdapter(),
            (type, adapter) -> journal.registerEntryAdapter(type, adapter));

    final MessageQueue queue = new AsyncMessageQueue(null);
    exchange = new LocalExchange(queue);
    exchangeReceivers = new ExchangeReceivers();

    exchange
      .register(Covey.of(
              new LocalExchangeSender(queue),
              exchangeReceivers.doStepTwoReceiver,
              new LocalExchangeAdapter<DoStepTwo,DoStepTwo>(DoStepTwo.class),
              DoStepTwo.class,
              DoStepTwo.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              new LocalExchangeSender(queue),
              exchangeReceivers.doStepThreeReceiver,
              new LocalExchangeAdapter<DoStepThree,DoStepThree>(DoStepThree.class),
              DoStepThree.class,
              DoStepThree.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              new LocalExchangeSender(queue),
              exchangeReceivers.doStepFourReceiver,
              new LocalExchangeAdapter<DoStepFour,DoStepFour>(DoStepFour.class),
              DoStepFour.class,
              DoStepFour.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              new LocalExchangeSender(queue),
              exchangeReceivers.markCompletedReceiver,
              new LocalExchangeAdapter<MarkCompleted,MarkCompleted>(MarkCompleted.class),
              MarkCompleted.class,
              MarkCompleted.class,
              LocalExchangeMessage.class));

    processTypeRegistry = new ProcessTypeRegistry(world);
    processTypeRegistry.register(new SourcedProcessInfo(FiveStepSourcedProcess.class, FiveStepSourcedProcess.class.getSimpleName(), exchange, sourcedTypeRegistry));

    process = world.actorFor(FiveStepProcess.class, FiveStepSourcedProcess.class);

    exchangeReceivers.process(process);
  }
}
