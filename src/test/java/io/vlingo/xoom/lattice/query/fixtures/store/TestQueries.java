package io.vlingo.xoom.lattice.query.fixtures.store;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.symbio.State.ObjectState;

import java.util.Collection;

public interface TestQueries {
  Completes<TestState> testStateById(final String id);

  Completes<TestState> testStateById(final String id, final TestState notFoundState);

  Completes<ObjectState<TestState>> testObjectStateById(final String id);

  Completes<ObjectState<TestState>> testObjectStateById(final String id, final ObjectState<TestState> notFoundState);

  Completes<Collection<TestState>> all(final Collection<TestState> all);
}