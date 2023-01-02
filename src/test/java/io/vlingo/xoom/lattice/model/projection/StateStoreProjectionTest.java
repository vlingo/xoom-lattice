// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.Outcome;
import io.vlingo.xoom.common.serialization.JsonSerialization;
import io.vlingo.xoom.lattice.model.projection.WarbleStateStoreProjection.Warble;
import io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry;
import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.State.TextState;
import io.vlingo.xoom.symbio.store.Result;
import io.vlingo.xoom.symbio.store.StorageException;
import io.vlingo.xoom.symbio.store.dispatch.NoOpDispatcher;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.xoom.symbio.store.state.inmemory.InMemoryStateStoreActor;

public class StateStoreProjectionTest {
  private Projection projection;
  private StateStore store;
  private Map<String,String> valueToProjectionId;
  private World world;

  @Test
  public void testThatProjectionsProject() {
    final CountingProjectionControl control = new CountingProjectionControl();

    final AccessSafely access = control.afterCompleting(3);

    projection.projectWith(textWarble("1", 1), control);
    projection.projectWith(textWarble("2", 2), control);
    projection.projectWith(textWarble("3", 3), control);

    final Map<String,Integer> confirmations = access.readFrom("confirmations");

    Assert.assertEquals(3, confirmations.size());

    Assert.assertEquals(1, valueOfProjectionIdFor("1", confirmations));
    Assert.assertEquals(1, valueOfProjectionIdFor("2", confirmations));
    Assert.assertEquals(1, valueOfProjectionIdFor("3", confirmations));

    Assert.assertEquals(3, (int) access.readFrom("sum"));
  }

  @Test
  public void testThatProjectionsUpdate() {
    final CountingProjectionControl control = new CountingProjectionControl();

    final AccessSafely accessControl = control.afterCompleting(6);

    projection.projectWith(textWarble("1", 1), control);
    projection.projectWith(textWarble("2", 2), control);
    projection.projectWith(textWarble("3", 3), control);

    projection.projectWith(textWarble("1", 4), control);
    projection.projectWith(textWarble("2", 5), control);
    projection.projectWith(textWarble("3", 6), control);

    final Map<String,Integer> confirmations = accessControl.readFrom("confirmations");

    Assert.assertEquals(6, confirmations.size());

    Assert.assertEquals(1, valueOfProjectionIdFor("1", confirmations));
    Assert.assertEquals(1, valueOfProjectionIdFor("2", confirmations));
    Assert.assertEquals(1, valueOfProjectionIdFor("3", confirmations));

    Assert.assertEquals(6, (int) accessControl.readFrom("sum"));

    final CountingReadResultInterest interest = new CountingReadResultInterest();

    final AccessSafely accessInterest = interest.afterCompleting(3);

    store.read("1", Warble.class, interest);
    store.read("2", Warble.class, interest);
    store.read("3", Warble.class, interest);

    final int sum = accessInterest.readFrom("sum");

    Assert.assertEquals(21, sum);

    final Warble warble1 = accessInterest.readFrom("warble", "1");
    Assert.assertEquals(5, warble1.count);

    final Warble warble2 = accessInterest.readFrom("warble", "2");
    Assert.assertEquals(7, warble2.count);

    final Warble warble3 = accessInterest.readFrom("warble", "3");
    Assert.assertEquals(9, warble3.count);
  }

  @Test
  public void testThatProjectionsWriteStateBeforeHandlingNextEvent() {
    final CountingProjectionControl control = new CountingProjectionControl();

    final AccessSafely accessControl = control.afterCompleting(3);

    projection.projectWith(textWarble("1", 1), control);
    projection.projectWith(textWarble("1", 2), control);
    projection.projectWith(textWarble("1", 3), control);

    final Map<String,Integer> confirmations = accessControl.readFrom("confirmations");

    Assert.assertEquals(3, confirmations.size());

    final CountingReadResultInterest interest = new CountingReadResultInterest();

    final AccessSafely accessInterest = interest.afterCompleting(1);

    store.read("1", Warble.class, interest);

    final Warble warble = accessInterest.readFrom("warble", "1");
    Assert.assertEquals(6, warble.count);
  }

  @Test
  public void testThatProjectionDoesNotRequireDiff() {
    final Projection projection = world.actorFor(Projection.class, WarbleStateStoreProjection.class, store, true);

    final CountingProjectionControl control = new CountingProjectionControl();

    final AccessSafely accessControl = control.afterCompleting(2);

    projection.projectWith(textWarble("1", 1), control);
    projection.projectWith(textWarble("1", 0), control);

    final Map<String,Integer> confirmations = accessControl.readFrom("confirmations");

    Assert.assertEquals(2, confirmations.size());

    final CountingReadResultInterest interest = new CountingReadResultInterest();

    final AccessSafely accessInterest = interest.afterCompleting(1);

    store.read("1", Warble.class, interest);

    final Warble warble = accessInterest.readFrom("warble", "1");
    Assert.assertEquals(1, warble.count);
  }

  @Test
  public void testThatProjectionDoesRequireDiff() {
    final Projection projection = world.actorFor(Projection.class, WarbleStateStoreProjection.class, store, false);

    final CountingProjectionControl control = new CountingProjectionControl();

    final AccessSafely accessControl = control.afterCompleting(3);

    projection.projectWith(textWarble("1", 1_000), control);
    projection.projectWith(textWarble("1", 1_000), control); // forces previousData answer to not write
    projection.projectWith(textWarble("1", 3_000), control);

    final Map<String,Integer> confirmations = accessControl.readFrom("confirmations");

    Assert.assertEquals(3, confirmations.size());

    final CountingReadResultInterest interest = new CountingReadResultInterest();

    final AccessSafely accessInterest = interest.afterCompleting(1);

    store.read("1", Warble.class, interest);

    final Warble warble = accessInterest.readFrom("warble", "1");
    Assert.assertEquals(4_000, warble.count); // 4_000 not 5_000
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-state-store-projection");

    store = world.actorFor(StateStore.class, InMemoryStateStoreActor.class, Arrays.asList(new NoOpDispatcher()));

    projection = world.actorFor(Projection.class, WarbleStateStoreProjection.class, store);

    StatefulTypeRegistry.registerAll(world, store, Warble.class);

    valueToProjectionId = new HashMap<>();
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  private Projectable textWarble(final String id, final int value) {
    final Warble warble = new Warble(id, "W" + value, value);

    final TextState state = new TextState(id, Warble.class, 1, JsonSerialization.serialized(warble), warble.version, Metadata.withObject(warble));

    final String valueText = Integer.toString(value);
    final String projectionId = UUID.randomUUID().toString();

    valueToProjectionId.put(valueText, projectionId);

    return new TextProjectable(state, Collections.emptyList(), projectionId);
  }

  private int valueOfProjectionIdFor(final String valueText, final Map<String,Integer> confirmations) {
    return confirmations.get(valueToProjectionId.get(valueText));
  }

  private static class CountingProjectionControl implements ProjectionControl {
    private AccessSafely access = AccessSafely.afterCompleting(0);

    private final Map<String, Integer> confirmations = new ConcurrentHashMap<>();

    @Override
    public void confirmProjected(final String projectionId) {
      access.writeUsing("confirmations", projectionId);
    }

    public AccessSafely afterCompleting(final int times) {
      access = AccessSafely.afterCompleting(times);

      access.writingWith("confirmations", (String projectionId) -> {
        final int count = confirmations.getOrDefault(projectionId, 0);
        confirmations.put(projectionId, count + 1);
      });

      access.readingWith("confirmations", () -> confirmations);

      access.readingWith("sum", () -> confirmations.values().stream().mapToInt(i -> i).sum());

      return access;
    }
  }

  private static class CountingReadResultInterest implements ReadResultInterest {
    private AccessSafely access = AccessSafely.afterCompleting(0);
    private AtomicInteger sum = new AtomicInteger(0);
    private final Map<String, Warble> warbles = new ConcurrentHashMap<>();

    @Override
    public <S> void readResultedIn(
            final Outcome<StorageException, Result> outcome,
            final String id,
            final S state,
            final int stateVersion,
            final Metadata metadata,
            final Object object) {

      access.writeUsing("warble", (Warble) state);
    }

    public AccessSafely afterCompleting(final int times) {
      access = AccessSafely.afterCompleting(times);

      access.writingWith("warble", (Warble warble) -> {
        sum.addAndGet(warble.count);
        warbles.put(warble.name, warble);
      });

      access.readingWith("sum", () -> sum.get());
      access.readingWith("warble", (id) -> warbles.get(id));

      return access;
    }
  }
}
