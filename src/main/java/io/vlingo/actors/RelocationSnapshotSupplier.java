package io.vlingo.actors;

import java.io.Serializable;

@FunctionalInterface
public interface RelocationSnapshotSupplier<T extends Serializable> {
  T provideRelocationSnapshot();
}
