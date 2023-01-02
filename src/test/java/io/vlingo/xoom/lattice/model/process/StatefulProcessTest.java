// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.process;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.message.AsyncMessageQueue;
import io.vlingo.xoom.common.message.MessageQueue;
import io.vlingo.xoom.lattice.exchange.Covey;
import io.vlingo.xoom.lattice.exchange.Exchange;
import io.vlingo.xoom.lattice.exchange.local.LocalExchange;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeAdapter;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeMessage;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeSender;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepFive;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepFour;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepOne;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepThree;
import io.vlingo.xoom.lattice.model.process.FiveStepProcess.DoStepTwo;
import io.vlingo.xoom.lattice.model.process.ProcessTypeRegistry.StatefulProcessInfo;
import io.vlingo.xoom.lattice.model.stateful.MockTextDispatcher;
import io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry;
import io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.inmemory.InMemoryStateStoreActor;

public class StatefulProcessTest {
  private MockTextDispatcher dispatcher;
  private Exchange exchange;
  private ExchangeReceivers exchangeReceivers;
  private LocalExchangeSender exchangeSender;
  private StateStore stateStore;
  private StatefulTypeRegistry statefulTypeRegistry;
  private FiveStepProcess process;
  private ProcessTypeRegistry processTypeRegistry;
  private World world;

  @Test
  public void testFiveStepEmittingProcess() {
    process = world.actorFor(FiveStepProcess.class, FiveStepEmittingStatefulProcess.class);
    exchangeReceivers.process(process);
    //final AccessSafely listenerAccess = listener.afterCompleting(4);
    dispatcher.afterCompleting(4);

    exchange.send(new DoStepOne());

    assertEquals(5, (int) exchangeReceivers.access.readFrom("stepCount"));

    assertEquals(5, (int) process.queryStepCount().await());

    //assertEquals(4, (int) listenerAccess.readFrom("entriesCount")); // stepFiveHappened() doesn't emit
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    world = World.startWithDefaults("five-step-process-test");

    dispatcher = new MockTextDispatcher();
    final MessageQueue queue = new AsyncMessageQueue(null);
    exchange = new LocalExchange(queue);
    stateStore = world.actorFor(StateStore.class, InMemoryStateStoreActor.class, Arrays.asList(dispatcher));
    EntryAdapterProvider.instance(world);
    statefulTypeRegistry = new StatefulTypeRegistry(world);

    final Info<StepCountState> stepCountStateInfo =
            new StatefulTypeRegistry.Info(
            stateStore,
            StepCountState.class,
            StepCountState.class.getSimpleName());

    statefulTypeRegistry.register(stepCountStateInfo);

    exchangeReceivers = new ExchangeReceivers();
    exchangeSender = new LocalExchangeSender(queue);

    registerExchangeCoveys();

    processTypeRegistry = new ProcessTypeRegistry(world);
    processTypeRegistry.register(new StatefulProcessInfo(FiveStepEmittingStatefulProcess.class, FiveStepEmittingStatefulProcess.class.getSimpleName(), exchange, statefulTypeRegistry));
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
}
