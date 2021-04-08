// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.Collection;
import java.util.Optional;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.symbio.Source;

/**
 * Defines the means of dispatching {@code Projectable} instances to {@code Projections}s
 * based on matching the {@code Projections}s that handle descriptive causes.
 */
public interface ProjectionDispatcher {
  /**
   * Use the {@code projection} to project a given {@code Projectable} state when {@code becauseOf} is matched
   * with the reasons of a given .
   * @param projection the Projection that may be used
   * @param becauseOf the String[] holding one or more reasons that projection is required
   */
  void projectTo(final Projection projection, final String[] becauseOf);

  /**
   * Declares the projection type that is dispatched for a given set of causes/reasons.
   */
  public static class ProjectToDescription {
    public final Class<? extends Actor> projectionType;
    public final String[] becauseOf;
    public final Optional<Object> constructionParameter;

    /**
     * Answer a new ProjectToDescription with {@code projectionType} for matches on types in {@code becauseOf}.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param becauseOf the {@code Class<? extends Source<?>>} causes/reasons that the projectionType handles
     * @return ProjectToDescription
     */
    @SuppressWarnings("unchecked")
    public static ProjectToDescription with(final Class<? extends Actor> projectionType, final Class<? extends Source<?>>... becauseOf) {
      return with(projectionType, Optional.empty(), becauseOf);
    }

    /**
     * Answer a new ProjectToDescription with {@code projectionType} for matches on types in {@code becauseOf}.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param constructionParameter the {@code Optional<Object>} to pass as the projectionType constructor parameter, or empty
     * @param becauseOf the {@code Class<? extends Source<?>>} causes/reasons that the projectionType handles
     * @return ProjectToDescription
     */
    @SuppressWarnings("unchecked")
    public static ProjectToDescription with(final Class<? extends Actor> projectionType, final Optional<Object> constructionParameter, final Class<? extends Source<?>>... becauseOf) {
      final String[] representations = new String[becauseOf.length];
      int index = 0;

      for (final Class<?> sourceType : becauseOf) {
        representations[index++] = sourceType.getName();
      }

      return new ProjectToDescription(projectionType, constructionParameter, representations);
    }

    /**
     * Answer a new ProjectToDescription with {@code projectionType} for matches in {@code packageContext}.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param contentsOf the Package used as a prefix wildcard that the projectionType handles
     * @return ProjectToDescription
     */
    public static ProjectToDescription with(final Class<? extends Actor> projectionType, final Package... contentsOf) {
      return with(projectionType, Optional.empty(), contentsOf);
    }

    /**
     * Answer a new ProjectToDescription with {@code projectionType} for matches in {@code packageContext}.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param constructionParameter the {@code Optional<Object>} to pass as the projectionType constructor parameter, or empty
     * @param contentsOf the Package used as a prefix wildcard that the projectionType handles
     * @return ProjectToDescription
     */
    public static ProjectToDescription with(final Class<? extends Actor> projectionType, final Optional<Object> constructionParameter, final Package... contentsOf) {
      final String[] representations = new String[contentsOf.length];
      int index = 0;

      for (final Package p : contentsOf) {
        representations[index++] = p.getName() + "*";
      }

      return new ProjectToDescription(projectionType, constructionParameter, representations);
    }

    /**
     * Answer a new ProjectToDescription with {@code projectionType} for matches in {@code becauseOf}.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param becauseOf the String[] causes/reasons that the projectionType handles
     * @return ProjectToDescription
     */
    public static ProjectToDescription with(final Class<? extends Actor> projectionType, final String... becauseOf) {
      return with(projectionType, Optional.empty(), becauseOf);
    }

    /**
     * Answer a new ProjectToDescription with {@code projectionType} for matches in {@code becauseOf}.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param constructionParameter the {@code Optional<Object>} to pass as the projectionType constructor parameter, or empty
     * @param becauseOf the String[] causes/reasons that the projectionType handles
     * @return ProjectToDescription
     */
    public static ProjectToDescription with(final Class<? extends Actor> projectionType, final Optional<Object> constructionParameter, final String... becauseOf) {
      return new ProjectToDescription(projectionType, constructionParameter, becauseOf);
    }

    /**
     * Construct my default state.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param becauseOf the String[] causes/reasons that the projectionType handles
     */
    public ProjectToDescription(final Class<? extends Actor> projectionType, final String... becauseOf) {
      this(projectionType, Optional.empty(), becauseOf);
    }

    /**
     * Construct my default state.
     * @param projectionType the Class of the projectionType that must be an Actor extender
     * @param constructionParameter the {@code Optional<Object>} to pass as the projectionType constructor parameter, or empty
     * @param becauseOf the String[] causes/reasons that the projectionType handles
     */
    public ProjectToDescription(final Class<? extends Actor> projectionType, final Optional<Object> constructionParameter, final String... becauseOf) {
      if (!Projection.class.isAssignableFrom(projectionType)) {
        throw new IllegalArgumentException("Class of projectionType must extend Actor and implement Projection.");
      }
      this.projectionType = projectionType;
      this.becauseOf = becauseOf;
      this.constructionParameter = constructionParameter;
    }
  }

  static class BinaryProjectionDispatcherInstantiator implements ActorInstantiator<BinaryProjectionDispatcherActor> {
    private static final long serialVersionUID = -8778106325710566773L;

    private final Collection<ProjectToDescription> projectToDescriptions;
    private final long multiConfirmationsExpiration;

    public BinaryProjectionDispatcherInstantiator(final Collection<ProjectToDescription> projectToDescriptions) {
      this(projectToDescriptions, MultiConfirming.DefaultExpirationLimit);
    }

    public BinaryProjectionDispatcherInstantiator(
            final Collection<ProjectToDescription> projectToDescriptions,
            final long multiConfirmationsExpiration) {
      this.projectToDescriptions = projectToDescriptions;
      this.multiConfirmationsExpiration = multiConfirmationsExpiration;
    }

    @Override
    public BinaryProjectionDispatcherActor instantiate() {
      return new BinaryProjectionDispatcherActor(projectToDescriptions, multiConfirmationsExpiration);
    }

    @Override
    public Class<BinaryProjectionDispatcherActor> type() {
      return BinaryProjectionDispatcherActor.class;
    }
  }

  static class TextProjectionDispatcherInstantiator implements ActorInstantiator<TextProjectionDispatcherActor> {
    private static final long serialVersionUID = -732875432321359779L;

    private final Collection<ProjectToDescription> projectToDescriptions;
    private final long multiConfirmationsExpiration;

    public TextProjectionDispatcherInstantiator(final Collection<ProjectToDescription> projectToDescriptions) {
      this(projectToDescriptions, MultiConfirming.DefaultExpirationLimit);
    }

    public TextProjectionDispatcherInstantiator(
            final Collection<ProjectToDescription> projectToDescriptions,
            final long multiConfirmationsExpiration) {

      this.projectToDescriptions = projectToDescriptions;
      this.multiConfirmationsExpiration = multiConfirmationsExpiration;
    }

    @Override
    public TextProjectionDispatcherActor instantiate() {
      return new TextProjectionDispatcherActor(projectToDescriptions, multiConfirmationsExpiration);
    }

    @Override
    public Class<TextProjectionDispatcherActor> type() {
      return TextProjectionDispatcherActor.class;
    }
  }
}
