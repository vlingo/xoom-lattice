// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Grid;
import io.vlingo.lattice.grid.example.Greeting;
import io.vlingo.lattice.grid.example.GreetingActor;
import io.vlingo.lattice.grid.example.Ponger;
import io.vlingo.lattice.grid.example.PongerActor;

public class Main  {
  public static void main(final String[] args) throws Exception {
    if (args.length == 1) {
      String nodeName = args[0];

      Grid grid = Grid.start("main-grid-world", Configuration.define(), ClusterProperties.allNodes(), nodeName);

      System.out.println("WAITING.....");
      Thread.sleep(30000);
      System.out.println("STARTING ACTORS");

//      Greeting greeting = bootstrap.grid.actorFor(Greeting.class, GreetingActor.class, nodeName);
//      while(true) {
//        greeting.respond("test")
//            .andThenConsume(System.out::println);
//      }

      Ponger ponger = grid.actorFor(Ponger.class, PongerActor.class);
      Greeting greeting = grid.actorFor(Greeting.class, GreetingActor.class, nodeName);
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
}
