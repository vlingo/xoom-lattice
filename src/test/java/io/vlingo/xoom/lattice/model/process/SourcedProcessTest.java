// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.message.AsyncMessageQueue;
import io.vlingo.xoom.common.message.MessageQueue;
import io.vlingo.xoom.lattice.exchange.Covey;
import io.vlingo.xoom.lattice.exchange.Exchange;
import io.vlingo.xoom.lattice.exchange.local.LocalExchange;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeAdapter;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeMessage;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeSender;
import io.vlingo.xoom.lattice.model.process.EntryAdapters.DoStepFiveAdapter;
import io.vlingo.xoom.lattice.model.process.EntryAdapters.DoStepFourAdapter;
import io.vlingo.xoom.lattice.model.process.EntryAdapters.DoStepOneAdapter;
import io.vlingo.xoom.lattice.model.process.EntryAdapters.DoStepThreeAdapter;
import io.vlingo.xoom.lattice.model.process.EntryAdapters.DoStepTwoAdapter;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepFive;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepFour;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepOne;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepThree;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepTwo;
import io.vlingo.xoom.lattice.model.process.ProcessTypeRegistry.SourcedProcessInfo;
import io.vlingo.xoom.lattice.model.sourcing.MockJournalDispatcher;
import io.vlingo.xoom.lattice.model.sourcing.Sourced;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.store.journal.Journal;
import io.vlingo.xoom.symbio.store.journal.inmemory.InMemoryJournal;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SourcedProcessTest {
  private Exchange exchange;
  private ExchangeReceivers exchangeReceivers;
  private LocalExchangeSender exchangeSender;
  private Journal<String> journal;
  private MockJournalDispatcher dispatcher;
  private FiveStepProcess process;
  private ProcessTypeRegistry processTypeRegistry;
  private SourcedTypeRegistry sourcedTypeRegistry;
  private World world;

  @Test
  public void testFiveStepSendingProcess() {
    process = world.actorFor(FiveStepProcess.class, FiveStepSendingSourcedProcess.class);
    exchangeReceivers.process(process);

    exchange.send(new DoStepOne());

    assertEquals(5, (int) exchangeReceivers.access.readFrom("stepCount"));

    assertEquals(5, (int) process.queryStepCount().await());
  }

  @Test
  public void testFiveStepEmittingProcess() {
    process = world.actorFor(FiveStepProcess.class, FiveStepEmittingSourcedProcess.class);
    exchangeReceivers.process(process);
    final AccessSafely listenerAccess = dispatcher.afterCompleting(4);

    exchange.send(new DoStepOne());

    assertEquals(5, (int) exchangeReceivers.access.readFrom("stepCount"));

    assertEquals(5, (int) process.queryStepCount().await());

    assertEquals(4, (int) listenerAccess.readFrom("entriesCount")); // stepFiveHappened() doesn't emit
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    world = World.startWithDefaults("five-step-process-test");

    final MessageQueue queue = new AsyncMessageQueue(null);
    exchange = new LocalExchange(queue);
    dispatcher = new MockJournalDispatcher();
    journal = new InMemoryJournal<>(dispatcher, world);

    sourcedTypeRegistry = new SourcedTypeRegistry(world);

    registerSourcedTypes(FiveStepSendingSourcedProcess.class);
    registerSourcedTypes(FiveStepEmittingSourcedProcess.class);

    exchangeReceivers = new ExchangeReceivers();
    exchangeSender = new LocalExchangeSender(queue);

    registerExchangeCoveys();

    processTypeRegistry = new ProcessTypeRegistry(world);
    processTypeRegistry.register(new SourcedProcessInfo(FiveStepSendingSourcedProcess.class, FiveStepSendingSourcedProcess.class.getSimpleName(), exchange, sourcedTypeRegistry));
    processTypeRegistry.register(new SourcedProcessInfo(FiveStepEmittingSourcedProcess.class, FiveStepEmittingSourcedProcess.class.getSimpleName(), exchange, sourcedTypeRegistry));
  }

  private void registerExchangeCoveys() {
    exchange
      .register(Covey.of(
              exchangeSender,
              exchangeReceivers.doStepOneReceiver,
              new LocalExchangeAdapter<DoStepOne,DoStepOne>(DoStepOne.class),
              DoStepOne.class,
              DoStepOne.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              exchangeSender,
              exchangeReceivers.doStepTwoReceiver,
              new LocalExchangeAdapter<DoStepTwo,DoStepTwo>(DoStepTwo.class),
              DoStepTwo.class,
              DoStepTwo.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              exchangeSender,
              exchangeReceivers.doStepThreeReceiver,
              new LocalExchangeAdapter<DoStepThree,DoStepThree>(DoStepThree.class),
              DoStepThree.class,
              DoStepThree.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              exchangeSender,
              exchangeReceivers.doStepFourReceiver,
              new LocalExchangeAdapter<DoStepFour,DoStepFour>(DoStepFour.class),
              DoStepFour.class,
              DoStepFour.class,
              LocalExchangeMessage.class))
      .register(Covey.of(
              exchangeSender,
              exchangeReceivers.doStepFiveReceiver,
              new LocalExchangeAdapter<DoStepFive,DoStepFive>(DoStepFive.class),
              DoStepFive.class,
              DoStepFive.class,
              LocalExchangeMessage.class));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <T extends Sourced<?>> void registerSourcedTypes(final Class<T> sourcedType) {
    EntryAdapterProvider entryAdapterProvider = EntryAdapterProvider.instance(world);

    sourcedTypeRegistry.register(new Info(journal, sourcedType, sourcedType.getSimpleName()));

    sourcedTypeRegistry.info(sourcedType)
      .registerEntryAdapter(ProcessMessage.class, new ProcessMessageTextAdapter(),
              (type, adapter) -> entryAdapterProvider.registerAdapter(type, adapter))
      .registerEntryAdapter(DoStepOne.class, new DoStepOneAdapter(),
              (type, adapter) -> entryAdapterProvider.registerAdapter(type, adapter))
      .registerEntryAdapter(DoStepTwo.class, new DoStepTwoAdapter(),
              (type, adapter) -> entryAdapterProvider.registerAdapter(type, adapter))
      .registerEntryAdapter(DoStepThree.class, new DoStepThreeAdapter(),
              (type, adapter) -> entryAdapterProvider.registerAdapter(type, adapter))
      .registerEntryAdapter(DoStepFour.class, new DoStepFourAdapter(),
              (type, adapter) -> entryAdapterProvider.registerAdapter(type, adapter))
      .registerEntryAdapter(DoStepFive.class, new DoStepFiveAdapter(),
            (type, adapter) -> entryAdapterProvider.registerAdapter(type, adapter));
  }
}
