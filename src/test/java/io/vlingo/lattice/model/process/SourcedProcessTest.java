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
import io.vlingo.actors.testkit.AccessSafely;
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
import io.vlingo.lattice.model.process.EntryAdapters.DoStepOneAdapter;
import io.vlingo.lattice.model.process.EntryAdapters.DoStepThreeAdapter;
import io.vlingo.lattice.model.process.EntryAdapters.DoStepTwoAdapter;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepFive;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepFour;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepOne;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepThree;
import io.vlingo.lattice.model.process.FiveStepProcess.DoStepTwo;
import io.vlingo.lattice.model.process.ProcessTypeRegistry.SourcedProcessInfo;
import io.vlingo.lattice.model.sourcing.Sourced;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.symbio.EntryAdapterProvider;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournal;

public class SourcedProcessTest {
  private Exchange exchange;
  private ExchangeReceivers exchangeReceivers;
  private LocalExchangeSender exchangeSender;
  private Journal<String> journal;
  private SendingJournalListener listener;
  private FiveStepProcess process;
  private ProcessTypeRegistry processTypeRegistry;
  private SourcedTypeRegistry sourcedTypeRegistry;
  private World world;

  @Test
  public void testFiveStepSendingProcess() {
    process = world.actorFor(FiveStepProcess.class, FiveStepSendingProcess.class);
    exchangeReceivers.process(process);

    exchange.send(new DoStepOne());

    assertEquals(5, (int) exchangeReceivers.access.readFrom("stepCount"));

    assertEquals(5, (int) process.queryStepCount().await());
  }

  @Test
  public void testFiveStepEmittingProcess() {
    process = world.actorFor(FiveStepProcess.class, FiveStepEmittingProcess.class);
    exchangeReceivers.process(process);
    final AccessSafely listenerAccess = listener.afterCompleting(4);

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
    listener = new SendingJournalListener(exchange, new ProcessMessageTextAdapter());
    journal = new InMemoryJournal<>(listener, world);

    sourcedTypeRegistry = new SourcedTypeRegistry(world);

    registerSourcedTypes(FiveStepSendingProcess.class);
    registerSourcedTypes(FiveStepEmittingProcess.class);

    exchangeReceivers = new ExchangeReceivers();
    exchangeSender = new LocalExchangeSender(queue);

    registerExchangeCoveys();

    processTypeRegistry = new ProcessTypeRegistry(world);
    processTypeRegistry.register(new SourcedProcessInfo(FiveStepSendingProcess.class, FiveStepSendingProcess.class.getSimpleName(), exchange, sourcedTypeRegistry));
    processTypeRegistry.register(new SourcedProcessInfo(FiveStepSendingProcess.class, FiveStepEmittingProcess.class.getSimpleName(), exchange, sourcedTypeRegistry));
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
