// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.lattice.grid.example.Greeting;
import io.vlingo.lattice.grid.example.GreetingActor;
import io.vlingo.lattice.grid.example.Ponger;
import io.vlingo.lattice.grid.example.PongerActor;

public class Main  {
  private static final Random random = new Random();
  private static AtomicInteger PORT_TO_USE = new AtomicInteger(10_000 + random.nextInt(50_000));

  public static void main(final String[] args) throws Exception {
    if (args.length == 1) {
      String nodeName = args[0];

      java.util.Properties properties = new java.util.Properties();

      properties.setProperty("cluster.ssl", "false");

      properties.setProperty("cluster.op.buffer.size", "4096");
      properties.setProperty("cluster.app.buffer.size", "10240");
      properties.setProperty("cluster.op.outgoing.pooled.buffers", "20");
      properties.setProperty("cluster.app.outgoing.pooled.buffers", "50");

      properties.setProperty("cluster.msg.charset", "UTF-8");

      properties.setProperty("cluster.app.class", "io.vlingo.cluster.model.application.FakeClusterApplicationActor");
      properties.setProperty("cluster.app.stage", "fake.app.stage");

      properties.setProperty("cluster.health.check.interval", "2000");
      properties.setProperty("cluster.live.node.timeout", "20000");
      properties.setProperty("cluster.heartbeat.interval", "7000");
      properties.setProperty("cluster.quorum.timeout", "60000");

      properties.setProperty("cluster.seedNodes", "node1,node2,node3");

      properties.setProperty("node.node1.id", "1");
      properties.setProperty("node.node1.name", "node1");
      properties.setProperty("node.node1.host", "localhost");
      properties.setProperty("node.node1.op.port", nextPortToUseString());
      properties.setProperty("node.node1.app.port", nextPortToUseString());

      properties.setProperty("node.node2.id", "2");
      properties.setProperty("node.node2.name", "node2");
      properties.setProperty("node.node2.host", "localhost");
      properties.setProperty("node.node2.op.port", nextPortToUseString());
      properties.setProperty("node.node2.app.port", nextPortToUseString());

      properties.setProperty("node.node3.id", "3");
      properties.setProperty("node.node3.name", "node3");
      properties.setProperty("node.node3.host", "localhost");
      properties.setProperty("node.node3.op.port", nextPortToUseString());
      properties.setProperty("node.node3.app.port", nextPortToUseString());

      final io.vlingo.cluster.model.Properties clusterProperties = io.vlingo.cluster.model.Properties.openForTest(properties);

      GridNodeBootstrap bootstrap = GridNodeBootstrap.boot(nodeName, clusterProperties);

      System.out.println("WAITING.....");
      Thread.sleep(30000);
      System.out.println("STARTING ACTORS");

//      Greeting greeting = bootstrap.grid.actorFor(Greeting.class, GreetingActor.class, nodeName);
//      while(true) {
//        greeting.respond("test")
//            .andThenConsume(System.out::println);
//      }

      Ponger ponger = bootstrap.grid().actorFor(Ponger.class, PongerActor.class);
      Greeting greeting = bootstrap.grid().actorFor(Greeting.class, GreetingActor.class, nodeName);
      greeting.respond(nodeName)
          .andThenConsume(pinger -> {
            System.out.println("staring on " + nodeName);
            pinger.ping(ponger, nodeName);
          });


    } else if (args.length > 1) {
      System.err.println("vlingo/lattice: Too many arguments; provide node name only.");
      System.exit(1);
    } else {
      System.err.println("vlingo/lattice: This node must be named with a command-line argument.");
      System.exit(1);
    }
  }

  private static int nextPortToUse() {
    return PORT_TO_USE.incrementAndGet();
  }

  private static String nextPortToUseString() {
    return "" + nextPortToUse();
  }
}
