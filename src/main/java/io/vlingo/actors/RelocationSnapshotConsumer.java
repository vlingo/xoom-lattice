package io.vlingo.actors;

@FunctionalInterface
public interface RelocationSnapshotConsumer<S> {
  void applyRelocationSnapshot(S snapshot);
}