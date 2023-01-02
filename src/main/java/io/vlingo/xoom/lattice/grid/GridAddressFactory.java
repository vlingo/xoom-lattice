// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid;

import java.util.UUID;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.UUIDAddressFactory;
import io.vlingo.xoom.common.identity.IdentityGeneratorType;

public final class GridAddressFactory extends UUIDAddressFactory {
  private static final Address None = new GridAddress(null, "(none)");

  public GridAddressFactory(final IdentityGeneratorType type) {
    super(type);
  }

  @Override
  public <T> Address findableBy(final T id) {
    return new GridAddress((UUID) id);
  }

  @Override
  public Address from(final long reservedId, final String name) {
    return new GridAddress(uuidFrom(reservedId), name);
  }

  @Override
  public Address from(final String idString) {
    return new GridAddress(UUID.fromString(idString));
  }

  @Override
  public Address from(final String idString, final String name) {
    return new GridAddress(UUID.fromString(idString), name);
  }

  @Override
  public Address unique() {
    return this.from(super.unique().idString());
  }

  @Override
  public Address uniqueWith(String name) {
    return this.from(super.uniqueWith(name).idString(), name);
  }

  @Override
  public Address uniquePrefixedWith(String prefixedWith) {
    return new GridAddress(super.uniquePrefixedWith(prefixedWith).idTyped(), prefixedWith, true);
  }

  @Override
  public Address none() {
    return None;
  }

  @Override
  public Address withHighId() {
    throw new UnsupportedOperationException("Unsupported for GridAddress.");
  }

  @Override
  public Address withHighId(final String name) {
    throw new UnsupportedOperationException("Unsupported for GridAddress.");
  }

  @Override
  public long testNextIdValue() {
    throw new UnsupportedOperationException("Unsupported for GridAddress.");
  }
}
