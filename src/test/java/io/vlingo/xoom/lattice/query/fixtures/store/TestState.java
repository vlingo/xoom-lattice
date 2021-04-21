package io.vlingo.xoom.lattice.query.fixtures.store;

public class TestState {
  public static final String MISSING = "(missing)";
  public final String name;

  private TestState(String name) {
    this.name = name;
  }

  public static TestState named(String name) {
    return new TestState(name);
  }

  public static TestState missing() {
    return new TestState(MISSING);
  }
}
