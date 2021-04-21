package io.vlingo.xoom.lattice.query.fixtures.store;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.query.StateStoreQueryActor;
import io.vlingo.xoom.symbio.State;
import io.vlingo.xoom.symbio.State.ObjectState;
import io.vlingo.xoom.symbio.store.state.StateStore;

import java.util.Collection;

public class TestQueriesActor extends StateStoreQueryActor implements TestQueries {
  public TestQueriesActor(StateStore stateStore) {
    super(stateStore);
  }

  public Completes<TestState> testStateById(final String id) {
    return queryStateFor(id, TestState.class);
  }

  public Completes<TestState> testStateById(final String id, final TestState notFoundState) {
    return queryStateFor(id, TestState.class, notFoundState);
  }

  @Override
  public Completes<ObjectState<TestState>> testObjectStateById(String id) {
    return queryObjectStateFor(id, TestState.class);
  }

  @Override
  public Completes<ObjectState<TestState>> testObjectStateById(String id, ObjectState<TestState> notFoundState) {
    return queryObjectStateFor(id, TestState.class, notFoundState);
  }

  @Override
  public Completes<Collection<TestState>> all(final Collection<TestState> all) {
    return streamAllOf(TestState.class, all);
  }
}