// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.query;

/**
 * An Exception used to indicate the failure of an attempt to {@code queryAll()}
 * or to {@code queryObject()}.
 */
public class ObjectQueryFailedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public final QueryAttempt<?,?,?> queryAttempt;

  public ObjectQueryFailedException(final QueryAttempt<?,?,?> queryAttempt) {
    super();

    this.queryAttempt = queryAttempt;
  }

  public ObjectQueryFailedException(final QueryAttempt<?,?,?> queryAttempt, final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

    this.queryAttempt = queryAttempt;
  }

  public ObjectQueryFailedException(final QueryAttempt<?,?,?> queryAttempt, final String message, final Throwable cause) {
    super(message, cause);

    this.queryAttempt = queryAttempt;
  }

  public ObjectQueryFailedException(final QueryAttempt<?,?,?> queryAttempt, final String message) {
    super(message);

    this.queryAttempt = queryAttempt;
  }

  public ObjectQueryFailedException(final QueryAttempt<?,?,?> queryAttempt, final Throwable cause) {
    super(cause);

    this.queryAttempt = queryAttempt;
  }

  @SuppressWarnings("unchecked")
  public <S,O,R> QueryAttempt<S,O,R> queryAttempt() {
    return (QueryAttempt<S,O,R>) queryAttempt;
  }
}
