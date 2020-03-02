package io.vlingo.lattice.grid;

import io.vlingo.lattice.grid.example.Greeting;
import io.vlingo.lattice.grid.example.GreetingActor;
import io.vlingo.lattice.grid.example.Ponger;
import io.vlingo.lattice.grid.example.PongerActor;

public class Main  {

  public static void main(final String[] args) throws Exception {
    if (args.length == 1) {
      String nodeName = args[0];
      GridNodeBootstrap bootstrap = GridNodeBootstrap.boot(nodeName);

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
}
