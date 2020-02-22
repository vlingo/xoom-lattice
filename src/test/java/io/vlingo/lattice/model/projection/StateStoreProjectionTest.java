// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.Outcome;
import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.lattice.model.projection.WarbleStateStoreProjection.Warble;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.dispatch.NoOpDispatcher;
import io.vlingo.symbio.store.state.StateStore;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.inmemory.InMemoryStateStoreActor;

public class StateStoreProjectionTest {
  private Projection projection;
  private StateStore store;
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

    Assert.assertEquals(1, (int) confirmations.get("1"));
    Assert.assertEquals(1, (int) confirmations.get("2"));
    Assert.assertEquals(1, (int) confirmations.get("3"));

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

    Assert.assertEquals(1, (int) confirmations.get("1"));
    Assert.assertEquals(1, (int) confirmations.get("2"));
    Assert.assertEquals(1, (int) confirmations.get("3"));

    Assert.assertEquals(6, (int) accessControl.readFrom("sum"));

    final CountingReadResultInterest interest = new CountingReadResultInterest();

    final AccessSafely accessInterest = interest.afterCompleting(3);

    store.read("1", Warble.class, interest);
    store.read("2", Warble.class, interest);
    store.read("3", Warble.class, interest);

    final int sum = accessInterest.readFrom("sum");

    Assert.assertEquals(15, sum);

    final Warble warble1 = accessInterest.readFrom("warble", "1");
    Assert.assertEquals(4, warble1.count);

    final Warble warble2 = accessInterest.readFrom("warble", "2");
    Assert.assertEquals(5, warble2.count);

    final Warble warble3 = accessInterest.readFrom("warble", "3");
    Assert.assertEquals(6, warble3.count);
}

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-state-store-projection");

    store = world.actorFor(StateStore.class, InMemoryStateStoreActor.class, Arrays.asList(new NoOpDispatcher()));

    projection = world.actorFor(Projection.class, WarbleStateStoreProjection.class, store);

    StatefulTypeRegistry.registerAll(world, store, Warble.class);
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  private Projectable textWarble(final String id, final int value) {
    final String valueText = Integer.toString(value);
    final Warble warble = new Warble(id, "W" + value, value);

    final TextState state = new TextState(id, Warble.class, 1, JsonSerialization.serialized(warble), warble.version, Metadata.withObject(warble));

    return new TextProjectable(state, Collections.emptyList(), valueText);
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
