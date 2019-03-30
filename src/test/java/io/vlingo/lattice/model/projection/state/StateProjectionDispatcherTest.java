// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection.state;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Protocols;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.lattice.model.projection.MockProjection;
import io.vlingo.lattice.model.projection.Projectable;
import io.vlingo.lattice.model.projection.Projection;
import io.vlingo.lattice.model.projection.ProjectionControl;
import io.vlingo.lattice.model.projection.ProjectionDispatcher;
import io.vlingo.lattice.model.projection.ProjectionDispatcher.ProjectToDescription;
import io.vlingo.lattice.model.projection.ProjectionDispatcherTest;
import io.vlingo.lattice.model.projection.state.DescribedProjection.Outcome;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.StateStore.Dispatcher;

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
            world.actorFor(Dispatcher.class, TextStateProjectionDispatcherActor.class, Arrays.asList(description));

    final Outcome outcome = new Outcome(2);
    dispatcher.controlWith(outcome);

    dispatcher.dispatch("123", new TextState("123", Object.class, 1, "blah1", 1, Metadata.with("", "op1")));
    dispatcher.dispatch("123", new TextState("123", Object.class, 1, "blah2", 1, Metadata.with("", "op2")));

    outcome.until.completes();

    assertEquals(2, outcome.count.get());
  }

  @Test
  public void testThatProjectionsPipeline() {
    final StateStore store = store();

    final FilterOutcome filterOutcome = new FilterOutcome(3);

    ProjectionDispatcher filter1 =
            FilterProjectionDispatcherActor.filterFor(world, projectionDispatcher, new String[] {"op-1"}, filterOutcome);

    ProjectionDispatcher filter2 =
            FilterProjectionDispatcherActor.filterFor(world, filter1, new String[] {"op-1"}, filterOutcome);

    FilterProjectionDispatcherActor.filterFor(world, filter2, new String[] {"op-1"}, filterOutcome);

    final Entity1 entity1 = new Entity1("123-1", 1);

    store.write(entity1.id, entity1, 1, Metadata.with("value1", "op-1"), new MockResultInterest(0));

    filterOutcome.until.completes();

    assertEquals(3, filterOutcome.filterCount.get());
  }

  @Override
  protected Class<? extends Dispatcher> dispatcherInterfaceClass() {
    return Dispatcher.class;
  }

  @Override
  protected Class<? extends Actor> projectionDispatcherClass() {
    return TextStateProjectionDispatcherActor.class;
  }

  @Override
  protected Class<? extends StateStore> stateStoreInterfaceClass() {
    return StateStore.class;
  }

  public static class FilterProjectionDispatcherActor extends StateProjectionDispatcherActor
      implements Projection, ProjectionDispatcher {

    private final FilterOutcome outcome;

    @Override
    public <S extends State<?>, C extends Source<?>> void dispatch(final String dispatchId, final S state, final Collection<C> sources) {
    }

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
      outcome.filterCount.incrementAndGet();
      control.confirmProjected(projectable.projectionId());
      dispatch(projectable.projectionId(), projectable);
      outcome.until.happened();
    }

    @Override
    protected boolean requiresDispatchedConfirmation() {
      return false;
    }
  }

  private static final class FilterOutcome {
    public final AtomicInteger filterCount;
    public final TestUntil until;

    FilterOutcome(final int testUntilHappenings) {
      this.filterCount = new AtomicInteger(0);
      this.until = TestUntil.happenings(testUntilHappenings);
    }
  }
}
