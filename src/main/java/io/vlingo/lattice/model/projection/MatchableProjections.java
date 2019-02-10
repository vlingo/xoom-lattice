// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maintains any number of {@code Projection}s that are used to project
 * when an {@code actualCause} happens. Supports simple wildcards.
 */
public class MatchableProjections {
  private static final String Wildcard = "*";

  private final Map<Cause, List<Projection>> mappedProjections;

  /**
   * Constructs my default state.
   */
  public MatchableProjections() {
    this.mappedProjections = new HashMap<>();
  }

  /**
   * Answer the {@code List<Projection>} matching the {@code actualCause} requiring projection(s).
   * @param actualCause the String describing why any number of {@code Projection}s are required.
   * @return {@code List<Projection>}
   */
  public List<Projection> matchProjections(final String actualCause) {
    return mappedProjections
      .keySet()
      .stream()
      .filter(cause -> cause.matches(actualCause))
      .map(cause -> mappedProjections.get(cause))
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  /**
   * Registers all causes, which may be combined and separated by semicolons (;) to
   * the projection such that later matches will be dispatched to the projection.
   * For example: final String whenMatchingCauses = "User:new;User:contact;User:name";
   * @param projection the Projection to which matches are dispatched
   * @param whenMatchingCauses the String[] with one or more cause patterns separated by semicolons
   */
  public void mayDispatchTo(final Projection projection, final String[] whenMatchingCauses) {
    for (final String whenMatchingCause : whenMatchingCauses) {
      final Cause cause = Cause.determineFor(whenMatchingCause);
      List<Projection> projections = mappedProjections.get(cause);

      if (projections == null) {
        projections = new ArrayList<>(1);
        mappedProjections.put(cause, projections);
      }

      projections.add(projection);
    }
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "MatchableProjections[" + mappedProjections + "]";
  }

  /**
   * Abstract base for kinds of wildcard matching of causes.
   */
  private static abstract class Cause {
    final String value;

    /**
     * Answer a {@code Cause} 
     * @param matchableCause
     * @return
     */
    static Cause determineFor(final String matchableCause) {
      final boolean beginsWithWildcard = matchableCause.startsWith(Wildcard);
      final boolean endsWithWildcard = matchableCause.endsWith(Wildcard);

      if (beginsWithWildcard && endsWithWildcard) return new ContainsCause(matchableCause);
      else if (beginsWithWildcard) return new EndsWithCause(matchableCause);
      else if (endsWithWildcard) return new BeginsWithCause(matchableCause);
      else return new EntireCause(matchableCause);
    }

    /**
     * Construct my default state.
     * @param value the String segment to match on, which may specify one or more wildcards
     */
    Cause(final String value) { this.value = value.replaceAll("\\*", ""); }

    /**
     * Answer whether or not I match the {@code actualCause}.
     * @param actualCause the String describing the cause to match on
     * @return boolean
     */
    abstract boolean matches(final String actualCause);

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override public boolean equals(final Object other) {
      if (other == null || other.getClass() != this.getClass()) return false;
      return value.equals(((Cause) other).value);
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override public int hashCode() { return 31 * value.hashCode(); }

    /*
     * @see java.lang.Object#toString()
     */
    @Override public String toString() { return getClass().getSimpleName() + "[value=" + value + "]"; }
  }

  /**
   * A cause that matches on the beginning of a description value.
   */
  private static class BeginsWithCause extends Cause {
    BeginsWithCause(final String value) { super(value); }
    boolean matches(final String actualCause) { return actualCause.startsWith(value); }
  }

  /**
   * A cause that matches on any segment of a description value.
   */
  private static class ContainsCause extends Cause {
    ContainsCause(final String value) { super(value); }
    boolean matches(final String actualCause) { return actualCause.contains(value); }
  }

  /**
   * A cause that matches on the end of a description value.
   */
  private static class EndsWithCause extends Cause {
    EndsWithCause(final String value) { super(value); }
    boolean matches(final String actualCause) { return actualCause.endsWith(value); }
  }

  /**
   * A cause that matches exactly a description value.
   */
  private static class EntireCause extends Cause {
    EntireCause(final String value) { super(value); }
    boolean matches(final String actualCause) { return actualCause.equals(value); }
  }
}
