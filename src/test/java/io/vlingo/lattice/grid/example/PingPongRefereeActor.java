package io.vlingo.lattice.grid.example;

import io.vlingo.actors.Definition;
import io.vlingo.actors.GridActor;
import io.vlingo.actors.StatelessGridActor;
import io.vlingo.common.Completes;

import java.io.Serializable;
import java.util.Collections;

public class PingPongRefereeActor extends StatelessGridActor implements PingPongReferee {

  @Override
  public Completes<Pinger> whistle(String name) {
    logger().info("Whistling {}", name);
    final Pinger pinger = childActorFor(Pinger.class,
        Definition.has(PingerActor.class, Definition.NoParameters));
    return answerFrom(Completes.withSuccess(pinger));
  }
}
