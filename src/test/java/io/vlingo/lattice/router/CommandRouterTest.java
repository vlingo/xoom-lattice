// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.router;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.common.Completes;
import io.vlingo.lattice.model.Command;
import io.vlingo.lattice.router.CommandRouter.Type;
import io.vlingo.lattice.router.Solver.Stuff;

public class CommandRouterTest {
  private Address address;
  private Completes<Stuff> completes;
  private World world;

  @Test
  public void testThatCommandIsHandled() throws Exception {
    final RoutableCommand<Solver,SolveStuff,Completes<Stuff>> command =
            RoutableCommand
              .speaks(Solver.class)
              .to(SolverActor.class)
              .at(address.idString())
              .delivers(SolveStuff.with("123", 21))
              .answers(completes)
              .handledBy(SolverHandler.newInstance(new Result()));

    command.handleWithin(world.stage());

    final Stuff stuff = completes.await();

    assertEquals(42, stuff.value);
  }

  @Test
  public void testThatPartitioningRouterRoutes() {
    final CommandRouter router = CommandRouter.of(world.stage(), Type.Partitioning, 5);

    final Result result = new Result();

    final RoutableCommand<Solver,SolveStuff,Completes<Stuff>> command =
            RoutableCommand
              .speaks(Solver.class)
              .to(SolverActor.class)
              .at(address.idString())
              .delivers(SolveStuff.with("123", 21))
              .answers(completes)
              .handledBy(SolverHandler.newInstance(result));

    router.route(command);

    final Stuff stuff = completes.await();

    assertEquals(42, stuff.value);
    assertEquals(1, result.countOf(0));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testThatPartitioningRouterPartitions() {
    final int totalPartitions = 5;
    final int totalTimes = totalPartitions * 2;

    final Completes<Stuff>[] completes = new Completes[totalTimes];

    final CommandRouter router = CommandRouter.of(world.stage(), Type.Partitioning, totalPartitions);

    final Result result = new Result();

    final SolverHandler[] solverHandlers = new SolverHandler[totalPartitions];

    for (int idx = 0; idx < totalPartitions; ++idx) {
      solverHandlers[idx] = SolverHandler.newInstance(result);
    }

    for (int idx = 0; idx < totalTimes; ++idx) {
      completes[idx] = Completes.using(world.stage().scheduler());

      final SolverHandler partitionSolverHandler = solverHandlers[idx % totalPartitions];

      final RoutableCommand<Solver,SolveStuff,Completes<Stuff>> command =
              RoutableCommand
                .speaks(Solver.class)
                .to(SolverActor.class)
                .at(address.idString())
                .delivers(SolveStuff.with("" + idx, 21))
                .answers(completes[idx])
                .handledBy(partitionSolverHandler);

      router.route(command);
    }

    for (int idx = 0; idx < totalTimes; ++idx) {
      final Stuff stuff = completes[idx].await();

      assertEquals(42, stuff.value);
    }

    for (int handlerId = 0; handlerId < totalPartitions; ++handlerId) {
      assertEquals(2, result.countOf(handlerId));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testThatRoundRobinRouterRoutesToAll() {
    final int totalRoutees = 5;
    final int totalTimes = totalRoutees * 3;

    final Completes<Stuff>[] completes = new Completes[totalTimes];

    final CommandRouter router = CommandRouter.of(world.stage(), Type.RoundRobin, totalRoutees);

    final Result result = new Result();

    final SolverHandler[] solverHandlers = new SolverHandler[totalRoutees];

    for (int idx = 0; idx < totalRoutees; ++idx) {
      solverHandlers[idx] = SolverHandler.newInstance(result);
    }

    for (int idx = 0; idx < totalTimes; ++idx) {
      completes[idx] = Completes.using(world.stage().scheduler());

      final SolverHandler roundRobinSolverHandler = solverHandlers[idx % totalRoutees];

      final RoutableCommand<Solver,SolveStuff,Completes<Stuff>> command =
              RoutableCommand
                .speaks(Solver.class)
                .to(SolverActor.class)
                .at(address.idString())
                .delivers(SolveStuff.with("" + idx, 21))
                .answers(completes[idx])
                .handledBy(roundRobinSolverHandler);

      router.route(command);
    }

    for (int idx = 0; idx < totalTimes; ++idx) {
      final Stuff stuff = completes[idx].await();

      assertEquals(42, stuff.value);
    }

    for (int handlerId = 0; handlerId < totalRoutees; ++handlerId) {
      assertEquals(3, result.countOf(handlerId));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testThatLoadBalancingRouterRoutesEvenly() {
    final int totalRoutees = 2;
    final int totalTimes = totalRoutees * 10;

    final Completes<Stuff>[] completes = new Completes[totalTimes];

    final CommandRouter router = CommandRouter.of(world.stage(), Type.LoadBalancing, totalRoutees);

    final Result result = new Result();

    final SolverHandler[] solverHandlers = new SolverHandler[totalRoutees];

    for (int idx = 0; idx < totalRoutees; ++idx) {
      solverHandlers[idx] = SolverHandler.newInstance(result);
    }

    for (int idx = 0; idx < totalTimes; ++idx) {
      completes[idx] = Completes.using(world.stage().scheduler());

      final SolverHandler loadBalancingSolverHandler = solverHandlers[idx % totalRoutees];

      final RoutableCommand<Solver,SolveStuff,Completes<Stuff>> command =
              RoutableCommand
                .speaks(Solver.class)
                .to(SolverActor.class)
                .at(address.idString())
                .delivers(SolveStuff.with("" + idx, 21))
                .answers(completes[idx])
                .handledBy(loadBalancingSolverHandler);

      router.route(command);
    }

    for (int idx = 0; idx < totalTimes; ++idx) {
      final Stuff stuff = completes[idx].await();

      assertEquals(42, stuff.value);
    }

    // NOTE: It is difficult to impossible to predict which of the
    // routees will have routed equal or more commands. Rather than
    // attempt that ensure that all commands have been handled.
    int totalCounts = 0;
    for (int handlerId = 0; handlerId < totalRoutees; ++handlerId) {
      totalCounts += result.countOf(handlerId);
    }
    assertEquals(totalTimes, totalCounts);
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-command-router");
    address = world.addressFactory().unique();
    world.stage().actorFor(Solver.class, Definition.has(SolverActor.class, Definition.NoParameters), address);
    completes = Completes.using(world.stage().scheduler());
  }

  public static class SolveStuff extends Command {
    public final String id;
    public final int value;

    public static SolveStuff with(final String id, final int value) {
      return new SolveStuff(id, value);
    }

    @Override
    public String id() {
      return id;
    }

    private SolveStuff(final String id, final int value) {
      this.id = id;
      this.value = value;
    }
  }

  public static class SolverHandler implements CommandDispatcher<Solver,SolveStuff,Completes<Stuff>> {
    private final int handlerId;
    private final Result result;

    public static SolverHandler newInstance(final Result result) {
      return new SolverHandler(result);
    }

    @Override
    public void accept(final Solver component, final SolveStuff command, final Completes<Stuff> answer) {
      result.countTimes(handlerId);
      component.solveStuff(command.value).andThenConsume(answer::with);
    }

    private SolverHandler(final Result result) {
      this.result = result;
      this.handlerId = result.nextHandlerId();
    }
  }

  public static class Result {
    private final AtomicInteger handlerId = new AtomicInteger(-1);
    private final List<Integer> times = new CopyOnWriteArrayList<>();

    public int nextHandlerId() {
      final int id = handlerId.incrementAndGet();
      times.add(0);
      return id;
    }

    public int countOf(final int handlerId) {
      return times.get(handlerId);
    }

    public void countTimes(final int handlerId) {
      final int count = times.get(handlerId);
      times.set(handlerId, count + 1);
    }
  }
}
