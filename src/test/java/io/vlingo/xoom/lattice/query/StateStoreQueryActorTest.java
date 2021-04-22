package io.vlingo.xoom.lattice.query;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.TestWorld;
import io.vlingo.xoom.common.Outcome;
import io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry;
import io.vlingo.xoom.lattice.query.fixtures.store.TestQueries;
import io.vlingo.xoom.lattice.query.fixtures.store.TestQueriesActor;
import io.vlingo.xoom.lattice.query.fixtures.store.TestState;
import io.vlingo.xoom.symbio.Source;
import io.vlingo.xoom.symbio.State.ObjectState;
import io.vlingo.xoom.symbio.store.Result;
import io.vlingo.xoom.symbio.store.StorageException;
import io.vlingo.xoom.symbio.store.dispatch.NoOpDispatcher;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.inmemory.InMemoryStateStoreActor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StateStoreQueryActorTest {
  private World world;
  private FailingStateStore stateStore;
  private TestQueries queries;

  @Test
  public void itFindsStateByIdAndType() {
    givenTestState("1", "Foo");

    TestState testState = queries.testStateById("1").await(100);

    assertEquals("Foo", testState.name);
  }

  @Test
  public void itReturnsNullIfStateIsNotFoundByIdAndType() {
    TestState testState = queries.testStateById("1").await(100);

    assertEquals(null, testState);
  }

  @Test
  public void itFindsStateByIdAndTypeWithNotFoundState() {
    givenTestState("1", "Foo");

    TestState testState = queries.testStateById("1", TestState.missing()).await(100);

    assertEquals("Foo", testState.name);
  }

  @Test
  public void itReturnsNotFoundStateIfStateIsNotFoundByIdAndType() {
    TestState testState = queries.testStateById("1", TestState.missing()).await(100);

    assertEquals(TestState.MISSING, testState.name);
  }

  @Test
  public void itFindsObjectStateByIdAndType() {
    givenTestState("1", "Foo");

    ObjectState<TestState> testState = queries.testObjectStateById("1").await(100);

    assertEquals(true, testState.isObject());
    assertEquals("Foo", ((TestState)testState.data).name);
  }

  @Test
  public void itReturnsNullObjectStateIfNotFoundByIdAndType() {
    ObjectState<TestState> testState = queries.testObjectStateById("1").await(100);

    assertEquals(ObjectState.Null, testState);
  }

  @Test
  public void itFindsObjectStateByIdAndTypeWithNotFoundObjectState() {
    givenTestState("1", "Foo");

    ObjectState<TestState> notFoundState = new ObjectState();

    ObjectState<TestState> testState = queries.testObjectStateById("1", notFoundState).await(100);

    assertEquals(true, testState.isObject());
    assertEquals("Foo", ((TestState)testState.data).name);
  }

  @Test
  public void itReturnsNullObjectStateIfNotFoundByIdAndTypeWithNotFoundObjectState() {
    ObjectState<TestState> notFoundState = new ObjectState();

    ObjectState<TestState> testState = queries.testObjectStateById("1", notFoundState).await(100);

    assertEquals(notFoundState, testState);
  }

  @Test
  public void itStreamsAllStatesByType() {
    givenTestState("1", "Foo");
    givenTestState("2", "Bar");
    givenTestState("3", "Baz");
    givenTestState("4", "Bam");

    List<TestState> allStates = new ArrayList<>();
    List<TestState> testStates = queries.all(allStates).await(100);

    assertEquals(4, allStates.size());
    assertEquals(4, testStates.size());
    assertEquals(allStates, testStates);
    assertEquals("Foo", testStates.get(0).name);
    assertEquals("Bar", testStates.get(1).name);
    assertEquals("Baz", testStates.get(2).name);
    assertEquals("Bam", testStates.get(3).name);
  }

  @Test
  public void itStreamsEmptyStore() {
    List<TestState> allStates = new ArrayList<>();
    List<TestState> testStates = queries.all(allStates).await(100);

    assertEquals(0, allStates.size());
    assertEquals(0, testStates.size());
  }

  @Test
  public void itFindsStateByIdAndTypeAfterRetries() {
    givenTestState("1", "Foo");
    givenStateReadFailures(3);

    TestState testState = queries.testStateById("1", 100, 3).await(500);

    assertEquals("Foo", testState.name);
  }

  @Test
  public void itFindsStateByIdAndTypeWithNotFoundStateAfterRetries() {
    givenTestState("1", "Foo");
    givenStateReadFailures(3);

    TestState testState = queries.testStateById("1", TestState.missing(), 100, 3).await(500);

    assertEquals("Foo", testState.name);
  }

  @Test
  public void itReturnsNotFoundStateIfStateIsNotFoundByIdAndTypeAfterRetries() {
    givenTestState("1", "Foo");
    givenStateReadFailures(3);

    TestState testState = queries.testStateById("1", TestState.missing(), 100, 2).await(500);

    assertEquals(TestState.MISSING, testState.name);
  }

  @Test
  public void itFindsObjectStateByIdAndTypeAfterRetries() {
    givenTestState("1", "Foo");
    givenStateReadFailures(3);

    ObjectState<TestState> testState = queries.testObjectStateById("1", 100, 3).await(500);

    assertEquals(true, testState.isObject());
    assertEquals("Foo", ((TestState)testState.data).name);
  }

  @Test
  public void itFindsObjectStateByIdAndTypeWithNotFoundStateAfterRetries() {
    givenTestState("1", "Foo");
    givenStateReadFailures(3);

    ObjectState<TestState> notFoundState = new ObjectState();

    ObjectState<TestState> testState = queries.testObjectStateById("1", notFoundState, 100, 3).await(500);

    assertEquals(true, testState.isObject());
    assertEquals("Foo", ((TestState)testState.data).name);
  }

  @Test
  public void itReturnsNullObjectStateIfStateIsNotFoundByIdAndTypeAfterRetries() {
    givenTestState("1", "Foo");
    givenStateReadFailures(3);

    ObjectState<TestState> notFoundState = new ObjectState();

    ObjectState<TestState> testState = queries.testObjectStateById("1", notFoundState, 100, 2).await(500);

    assertEquals(notFoundState, testState);
  }

  @Before
  public void init() {
    TestWorld.startWithDefaults("test-state-store-query");
    world = World.startWithDefaults("test-state-store-query");
    stateStore = new FailingStateStore(world.actorFor(StateStore.class, InMemoryStateStoreActor.class, Arrays.asList(new NoOpDispatcher())));
    StatefulTypeRegistry.registerAll(world, stateStore, TestState.class);
    queries = world.actorFor(TestQueries.class, TestQueriesActor.class, stateStore);
  }

  @After
  public void shutdown() {
    if (world != null) {
      world.terminate();
    }
  }

  private void givenStateReadFailures(int failures) {
    stateStore.expectReadFailures(failures);
  }

  private void givenTestState(String id, String name) {
    stateStore.write(
            id,
            TestState.named(name),
            1,
            new NoOpWriteResultInterest()
    );
  }

  private class NoOpWriteResultInterest implements StateStore.WriteResultInterest {
    @Override
    public <S, C> void writeResultedIn(Outcome<StorageException, Result> outcome, String s, S s1, int i, List<Source<C>> list, Object o) {
    }
  }
}
