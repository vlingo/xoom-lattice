package io.vlingo.lattice.model;

import io.vlingo.actors.StatelessGridActor;

public abstract class EntityGridActor extends StatelessGridActor {
  
  @Override
  protected final void resumeFromRelocation() {
    restore();
    super.resumeFromRelocation();
  }

  protected abstract void restore();
}
