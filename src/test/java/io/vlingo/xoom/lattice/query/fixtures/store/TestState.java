package io.vlingo.xoom.lattice.query.fixtures.store;

public class TestState {
  public static final String MISSING = "(missing)";
  public final String name;

  private TestState(final String name) {
    this.name = name;
  }

  public static TestState named(final String name) {
    return new TestState(name);
  }

  public static TestState missing() {
    return new TestState(MISSING);
  }
}
