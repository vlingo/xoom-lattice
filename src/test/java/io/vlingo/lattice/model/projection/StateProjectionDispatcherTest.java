// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Protocols;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.lattice.model.projection.DescribedProjection.Outcome;
import io.vlingo.lattice.model.projection.ProjectionDispatcher.ProjectToDescription;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.dispatch.Dispatchable;
import io.vlingo.symbio.store.dispatch.Dispatcher;
import io.vlingo.symbio.store.state.StateStore;

@SuppressWarnings({"rawtypes","unchecked"})
public class StateProjectionDispatcherTest extends ProjectionDispatcherTest {

  @Test
  public void testThatTextStateDataProjects() {
    final StateStore store = store();

    final MockResultInterest interest = new MockResultInterest(0);

    final MockProjection projection = new MockProjection();

    final AccessSafely access = projection.afterCompleting(2);

    projectionDispatcher.projectTo(projection, new String[] { "op1" });
    projectionDispatcher.projectTo(projection, new String[] { "op2" });

    final Entity1 entity1 = new Entity1("123-1", 1);
    final Entity1 entity2 = new Entity1("123-2", 2);

    store.write(entity1.id, entity1, 1, Metadata.with("value1", "op1"), interest);
    store.write(entity2.id, entity2, 1, Metadata.with("value2", "op2"), interest);

    assertEquals(2, (int) access.readFrom("projections"));
    assertEquals("123-1", access.readFrom("projectionId", 0));
    assertEquals("123-2", access.readFrom("projectionId", 1));
  }

  @Test
  public void testThatDescribedProjectionsRegister() {
    final ProjectToDescription description = new ProjectToDescription(DescribedProjection.class, "op1", "op2");

    final Dispatcher dispatcher =
            world.actorFor(Dispatcher.class, TextProjectionDispatcherActor.class, Collections.singletonList(description));

    final Outcome outcome = new Outcome(2);
    final AccessSafely accessOutcome = outcome.afterCompleting(2);
    dispatcher.controlWith(outcome);

    final TextState state = new TextState("123", Object.class, 1, "blah1", 1, Metadata.with("", "op1"));
    dispatcher.dispatch(new Dispatchable<>("123", LocalDateTime.now(), state, Collections.emptyList()));

    final TextState state2 = new TextState("1235", Object.class, 1, "blah2", 1, Metadata.with("", "op2"));
    dispatcher.dispatch(new Dispatchable<>("1235", LocalDateTime.now(), state2, Collections.emptyList()));

    assertEquals(2, (int) accessOutcome.readFrom("count"));
  }

  @Test
  public void testThatProjectionsPipeline() {
    final StateStore store = store();

    final FilterOutcome filterOutcome = new FilterOutcome();
    final AccessSafely filterOutcomeAccess = filterOutcome.afterCompleting(3);

    ProjectionDispatcher filter1 =
            FilterProjectionDispatcherActor.filterFor(world, projectionDispatcher, new String[] {"op-1"}, filterOutcome);

    ProjectionDispatcher filter2 =
            FilterProjectionDispatcherActor.filterFor(world, filter1, new String[] {"op-1"}, filterOutcome);

    FilterProjectionDispatcherActor.filterFor(world, filter2, new String[] {"op-1"}, filterOutcome);

    final Entity1 entity1 = new Entity1("123-1", 1);

    store.write(entity1.id, entity1, 1, Metadata.with("value1", "op-1"), new MockResultInterest(0));

    assertEquals(3, (int) filterOutcomeAccess.readFrom("filterCount"));
  }

  @Override
  protected Class<? extends Dispatcher> dispatcherInterfaceClass() {
    return Dispatcher.class;
  }

  @Override
  protected Class<? extends Actor> projectionDispatcherClass() {
    return TextProjectionDispatcherActor.class;
  }

  @Override
  protected Class<? extends StateStore> stateStoreInterfaceClass() {
    return StateStore.class;
  }

  public static class FilterProjectionDispatcherActor extends ProjectionDispatcherActor<Entry<?>,State<?>>
      implements Projection, ProjectionDispatcher {

    private final FilterOutcome outcome;

    public static ProjectionDispatcher filterFor(
            final World world,
            final ProjectionDispatcher projectionDispatcher,
            final String[] becauseOf,
            final FilterOutcome filterOutcome) {

      final Protocols projectionProtocols =
              world.actorFor(
                      new Class<?>[] { ProjectionDispatcher.class, Projection.class },
                      FilterProjectionDispatcherActor.class,
                      filterOutcome);

      final Protocols.Two<ProjectionDispatcher, Projection> projectionFilter = Protocols.two(projectionProtocols);

      projectionDispatcher.projectTo(projectionFilter._2, becauseOf);

      return projectionFilter._1;
    }

    public FilterProjectionDispatcherActor(final FilterOutcome outcome) {
      this.outcome = outcome;
    }

    @Override
    public void projectWith(final Projectable projectable, final ProjectionControl control) {
      outcome.increment();
      control.confirmProjected(projectable.projectionId());
      dispatch(projectable.projectionId(), projectable);
    }

    @Override
    protected boolean requiresDispatchedConfirmation() {
      return false;
    }

    @Override
    public void dispatch(final Dispatchable<Entry<?>, State<?>> dispatchable) {

    }

    @Override
    public void projectTo(final Projection projection, final String[] becauseOf) {
      outcome.increment();
    }
  }

  private static final class FilterOutcome {
    public final AtomicInteger filterCount;
    private AccessSafely access = AccessSafely.afterCompleting(0);

    FilterOutcome() {
      this.filterCount = new AtomicInteger(0);
    }

    public void increment() {
      access.writeUsing("filterCount", 1);
    }

    public AccessSafely afterCompleting(final int times) {
      access = AccessSafely.afterCompleting(times);
      access
        .writingWith("filterCount", (Integer increment) -> filterCount.addAndGet(increment))
        .readingWith("filterCount", () -> filterCount.get());

      return access;
    }
  }
}
