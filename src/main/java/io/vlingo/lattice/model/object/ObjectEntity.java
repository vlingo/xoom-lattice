// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import java.util.function.Supplier;

import io.vlingo.actors.Actor;
import io.vlingo.common.Outcome;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.object.ObjectStore.PersistResultInterest;
import io.vlingo.symbio.store.object.ObjectStore.QueryMultiResults;
import io.vlingo.symbio.store.object.ObjectStore.QueryResultInterest;
import io.vlingo.symbio.store.object.ObjectStore.QuerySingleResult;

/**
 * Abstract base type used to preserve and restore object state
 * by means of the {@code ObjectStore}. The {@code ObjectStore}
 * is typically backed by some form of object-relational mapping,
 * whether formally or informally implemented.
 */
public abstract class ObjectEntity<S,R extends State<?>> extends Actor 
  implements PersistResultInterest, QueryResultInterest {

  /**
   * Construct my default state.
   */
  protected ObjectEntity() {
    
  }

  /**
   * Answer my unique identity, which much be provided by
   * my concrete extender by overriding.
   * @return String
   */
  protected abstract String id();

  /**
   * Preserve my current state dispatching to {@code state(final S state)} when completed
   * and supply an eventual outcome by means of the given {@code andThen} function.
   * @param state the Object state to preserve
   * @param andThen the {@code Supplier<RT>} that will provide the fully updated state following this operation,
   * and which will used to answer an eventual outcome to the client of this entity
   * @param <RT> the return type of the Supplier function, which is the type of the completed state
   */
  protected <RT> void preserve(final Object state, final Supplier<RT> andThen) {
    
  }

  /**
   * Restore my current state, dispatching to {@code state(final S state)} when completed.
   */
  protected void restore() {
    
  }

  //=====================================
  // FOR INTERNAL USE ONLY.
  //=====================================

  /*
   * @see io.vlingo.symbio.store.object.ObjectStore.QueryResultInterest#queryAllResultedIn(io.vlingo.common.Outcome, io.vlingo.symbio.store.object.ObjectStore.QueryMultiResults, java.lang.Object)
   */
  @Override
  final public void queryAllResultedIn(final Outcome<StorageException, Result> outcome, final QueryMultiResults results, final Object object) {
    throw new UnsupportedOperationException("Must be unreachable: queryAllResultedIn()");
  }

  /*
   * @see io.vlingo.symbio.store.object.ObjectStore.QueryResultInterest#queryObjectResultedIn(io.vlingo.common.Outcome, io.vlingo.symbio.store.object.ObjectStore.QuerySingleResult, java.lang.Object)
   */
  @Override
  final public void queryObjectResultedIn(
          final Outcome<StorageException, Result> outcome,
          final QuerySingleResult result,
          final Object object) {
    
  }

  /*
   * @see io.vlingo.symbio.store.object.ObjectStore.PersistResultInterest#persistResultedIn(io.vlingo.common.Outcome, java.lang.Object, int, int, java.lang.Object)
   */
  @Override
  final public void persistResultedIn(
          final Outcome<StorageException, Result> outcome,
          final Object persistentObject,
          final int possible,
          final int actual,
          final Object object) {
    
  }
}
