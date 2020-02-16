// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.projection;

import java.util.Collection;
import java.util.Optional;

import io.vlingo.symbio.Entry;

/**
 * Provides parts of the state to be projected.
 */
public interface Projectable {
  /**
   * Answer my reason(s) for projection.
   * @return String[]
   */
  String[] becauseOf();

  /**
   * Answer my state as binary/bytes.
   * @return byte[]
   */
  byte[] dataAsBytes();

  /**
   * Answer my state as text.
   * @return String
   */
  String dataAsText();

  /**
   * Answer the version of my data.
   * @return int
   */
  int dataVersion();

  /**
   * Answer the unique id of my data.
   * @return String
   */
  String dataId();

  /**
   * Answer the {@code Collection<Entry<?>>} instances.
   * @return {@code Collection<Entry<?>>}
   */
  Collection<Entry<?>> entries();

  /**
   * Answer whether or not there are entries.
   * @return boolean
   */
  boolean hasEntries();

  /**
   * Answer my associated metadata.
   * @return String
   */
  String metadata();

  /**
   * Answer whether or no my {@code object} exists.
   * @return boolean
   */
  public boolean hasObject();

  /**
   * Answer my data as a specific {@code T} typed object.
   * @param <T> the type expected of the object
   * @return T
   */
  <T> T object();

  /**
   * Answer my data as an {@code Optional}.
   * @param <T> the value type
   * @return {@code Optional<T>}
   */
  <T> Optional<T> optionalObject();

  /**
   * Answer the unique identity associated with the projection operation.
   * @return String
   */
  String projectionId();

  /**
   * Answer whether or not {@code state} is non-null.
   * @return boolean
   */
  boolean hasState();

  /**
   * Answer my type as a {@code String}.
   * @return String
   */
  String type();

  /**
   * Answer the version of my type.
   * @return String
   */
  int typeVersion();
}
