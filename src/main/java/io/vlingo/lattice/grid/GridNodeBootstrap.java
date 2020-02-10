// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import io.vlingo.actors.Grid;
import io.vlingo.actors.Logger;
import io.vlingo.actors.World;
import io.vlingo.cluster.model.Cluster;
import io.vlingo.cluster.model.ClusterSnapshotControl;
import io.vlingo.cluster.model.Properties;
import io.vlingo.cluster.model.application.ClusterApplication.ClusterApplicationInstantiator;
import io.vlingo.common.Tuple2;
import io.vlingo.lattice.grid.example.Greeting;
import io.vlingo.lattice.grid.example.GreetingActor;

public class GridNodeBootstrap {
  private static GridNodeBootstrap instance;

  private final GridShutdownHook shutdownHook;
  private final World world;
  private final Grid grid;

  public static void main(final String[] args) throws Exception {
    if (args.length == 1) {
      String nodeName = args[0];
      GridNodeBootstrap bootstrap = boot(nodeName);

      System.out.println("WAITING.....");
      Thread.sleep(30000);
      System.out.println("STARTING ACTORS");
      Greeting greeting = bootstrap.grid.actorFor(Greeting.class, GreetingActor.class, nodeName);
      System.out.println("STARTED ACTORS");

      while(true) {
        greeting.respond("test")
            .andThenConsume(System.out::println);
        Thread.sleep(4000);
      }

//      Pinger pinger = bootstrap.grid.actorFor(Pinger.class, PingerActor.class);
//      Ponger ponger = bootstrap.grid.actorFor(Ponger.class, PongerActor.class);
//
//      pinger.ping(ponger);


    } else if (args.length > 1) {
      System.err.println("vlingo/lattice: Too many arguments; provide node name only.");
      System.exit(1);
    } else {
      System.err.println("vlingo/lattice: This node must be named with a command-line argument.");
      System.exit(1);
    }
  }

  public static GridNodeBootstrap boot(final String nodeName) throws Exception {
    final Grid grid = Grid.start("vlingo-lattice-grid", nodeName);
    return boot(grid.world(), grid, nodeName, false);
  }

  public static GridNodeBootstrap boot(final World world, final Grid grid, final String nodeName, final boolean embedded) throws Exception {
    final boolean mustBoot = GridNodeBootstrap.instance == null || !Cluster.isRunning();

    if (mustBoot) {
      Properties.instance.validateRequired(nodeName);

      final Tuple2<ClusterSnapshotControl, Logger> control =
              Cluster.controlFor(
                      world,
                      new GridNodeInstantiator(grid),
                      io.vlingo.cluster.model.Properties.instance,
                      nodeName);

      GridNodeBootstrap.instance = new GridNodeBootstrap(control, nodeName, world, grid);

      control._2.info("Successfully started cluster node: '" + nodeName + "'");

      if (!embedded) {
        control._2.info("==========");
      }
    }

    return GridNodeBootstrap.instance;
  }

  public static boolean exists() {
    return instance != null;
  }

  public static GridNodeBootstrap instance() {
    return instance;
  }

  public static void reset() {
    if (GridNodeBootstrap.instance != null) {
      Cluster.reset();
      GridNodeBootstrap.instance = null;
    }
  }

  private GridNodeBootstrap(final Tuple2<ClusterSnapshotControl, Logger> control, final String nodeName, World world, Grid grid) throws Exception {
    this.world = world;
    this.grid = grid;

    this.shutdownHook = new GridShutdownHook(nodeName, control);
    this.shutdownHook.register();
  }

  private static class GridNodeInstantiator extends ClusterApplicationInstantiator<GridNode> {

    private final Grid grid;

    public GridNodeInstantiator(Grid grid) {
      super(GridNode.class);
      this.grid = grid;
    }

    @Override
    public GridNode instantiate() {
      grid.setNodeId(node().id());
      return new GridNode(grid, node());
    }
  }
}
