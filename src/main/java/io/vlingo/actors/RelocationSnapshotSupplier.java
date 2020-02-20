package io.vlingo.actors;

@FunctionalInterface
public interface RelocationSnapshotSupplier<S> {
  S provideRelocationSnapshot();
}
