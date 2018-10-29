// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import java.util.UUID;

import io.vlingo.actors.Address;
import io.vlingo.actors.AddressFactory;
import io.vlingo.common.identity.IdentityGenerator;
import io.vlingo.common.identity.IdentityGeneratorType;

public final class GridAddressFactory implements AddressFactory {
  private static final Address None = new GridAddress(null, "(none)");

  private final IdentityGenerator generator;
  private final IdentityGeneratorType type;

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

  public Address none() {
    return None;
  }

  @Override
  public Address unique() {
    return new GridAddress(generator.generate());
  }

  @Override
  public Address uniquePrefixedWith(final String prefixedWith) {
    return new GridAddress(generator.generate(), prefixedWith, true);
  }

  @Override
  public Address uniqueWith(final String name) {
    return new GridAddress(generator.generate(name), name);
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

  GridAddressFactory(final IdentityGeneratorType type) {
    this.type = type;
    this.generator = this.type.generator();
  }

  private UUID uuidFrom(final long id) {
    return new UUID(Long.MAX_VALUE, id);
  }
}
