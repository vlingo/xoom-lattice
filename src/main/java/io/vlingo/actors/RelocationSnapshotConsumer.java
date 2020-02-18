package io.vlingo.actors;

import java.io.Serializable;

@FunctionalInterface
public interface RelocationSnapshotConsumer<T extends Serializable> {
  void applyRelocationSnapshot(T snapshot);
}