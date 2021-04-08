// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.query;

import io.vlingo.xoom.actors.CompletionTranslator;
import io.vlingo.xoom.symbio.store.QueryExpression;

/**
 * The elements used in the attempted {@code queryAll()} or {@code queryObject()}.
   * @param <S> the type of the StateObject being queried
   * @param <O> the type of the outcome of the query
   * @param <R> the final result, being a {@code S} or {@code List<S>}
 */
public class QueryAttempt<S,O,R> {
  public enum Cardinality { All, Object };

  public final Cardinality cardinality;
  public final CompletionTranslator<O,R> completionTranslator;
  public final QueryExpression query;
  public final Class<S> stateObjectType;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <ST,OT,RT> QueryAttempt<ST,OT,RT> from(final Object attempt) {
    final QueryAttempt typed = (QueryAttempt) attempt;
    return typed;
  }

  public static <ST,OT,RT> QueryAttempt<ST,OT,RT> with(final Cardinality cardinality, final Class<ST> stateObjectType, final QueryExpression query, final CompletionTranslator<OT,RT> completionTranslator) {
    return new QueryAttempt<>(cardinality, stateObjectType, query, completionTranslator);
  }

  public QueryAttempt(final Cardinality cardinality, final Class<S> stateObjectType, final QueryExpression query, final CompletionTranslator<O,R> completionTranslator) {
    this.cardinality = cardinality;
    this.stateObjectType = stateObjectType;
    this.query = query;
    this.completionTranslator = completionTranslator;
  }
}
