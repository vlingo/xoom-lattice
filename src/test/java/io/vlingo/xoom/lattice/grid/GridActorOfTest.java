// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.NoProtocol;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.actors.testkit.TestWorld;
import io.vlingo.xoom.cluster.ClusterProperties;

public class GridActorOfTest {
  private Grid grid;
  protected World world;
  protected TestWorld testWorld;

  @Test
  public void testThatNonExistingActorCreates() {
    final Address address = grid.addressFactory().unique();

    UUID.fromString(address.idString()); // asserts id is UUID

    final AtomicInteger valueHolder = new AtomicInteger(0);
    final AccessSafely access = AccessSafely.afterCompleting(1);
    access.writingWith("value", (Integer value) -> valueHolder.set(value));
    access.readingWith("value", () -> valueHolder.get());

    final NoProtocol proxy1 = grid.actorOf(NoProtocol.class, address).await();

    Assert.assertNull(proxy1);
    Assert.assertEquals(0, valueHolder.get());

    final NoProtocol proxy2 = grid.actorOf(NoProtocol.class, address, NoExistingActor.class, 1, access).await();

    final int value = access.readFrom("value");

    Assert.assertNotNull(proxy2);
    Assert.assertEquals(1, value);
    Assert.assertEquals(value, valueHolder.get());
  }

  @Test
  public void testThatActorOfAddressIsPingedByThreeClients() {
    final Address address = grid.addressFactory().unique();

    UUID.fromString(address.idString()); // asserts id is UUID

    final AtomicInteger valueHolder = new AtomicInteger(0);

    final AccessSafely access = AccessSafely.afterCompleting(3);

    access.writingWith("value", (Integer value) -> valueHolder.set(value));
    access.readingWith("value", () -> valueHolder.get());

    final RingDing proxy1 = grid.actorOf(RingDing.class, address, RingDingActor.class, access).await();
    Assert.assertNotNull(proxy1);
    proxy1.ringDing();

    final RingDing proxy2 = grid.actorOf(RingDing.class, address, RingDingActor.class, access).await();
    Assert.assertNotNull(proxy2);
    proxy2.ringDing();

    final RingDing proxy3 = grid.actorOf(RingDing.class, address, RingDingActor.class, access).await();
    Assert.assertNotNull(proxy3);
    proxy3.ringDing();

    final int value = access.readFrom("value");

    Assert.assertEquals(3, value);
    Assert.assertEquals(value, valueHolder.get());
  }

  @Before
  public void setUp() throws Exception {
    Configuration configuration =
            Configuration
              .define()
              .with(Slf4jLoggerPlugin
                      .Slf4jLoggerPluginConfiguration
                      .define()
                      .defaultLogger()
                      .name("xoom-actors"));

      testWorld = TestWorld.start("test", configuration);
      world = testWorld.world();

      final io.vlingo.xoom.cluster.model.Properties properties = ClusterProperties.oneNode();

      grid = Grid.start(world, properties, "node1");
      grid.quorumAchieved();
  }

  @After
  public void tearDown() throws Exception {
    testWorld.terminate();
  }

  public static interface RingDing {
    void ringDing();
  }

  public static class RingDingActor extends Actor implements RingDing {
    private final AccessSafely access;
    private int value;

    public RingDingActor(final AccessSafely access) {
      this.access = access;
      this.value = 0;

      assert stage() instanceof Grid;
    }

    @Override
    public void ringDing() {
      access.writeUsing("value", ++value);
    }
  }

  public static class NoExistingActor extends Actor implements NoProtocol {
    public NoExistingActor(final int value, final AccessSafely access) {
      access.writeUsing("value", value);

      assert stage() instanceof Grid;
    }
  }
}
