package io.vlingo.actors;

import java.io.Serializable;

public abstract class StatelessGridActor
    extends GridActor<StatelessGridActor.Null> {

  @Override
  public void applyRelocationSnapshot(Null snapshot) { }

  @Override
  public Null provideRelocationSnapshot() { return null; }

  public interface Null extends Serializable { }
}
