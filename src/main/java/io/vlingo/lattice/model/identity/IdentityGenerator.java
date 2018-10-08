// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.identity;

import java.security.SecureRandom;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public interface IdentityGenerator {
  UUID generate();
  UUID generate(final String name);

  static class NameBasedIdentityGenerator implements IdentityGenerator {
    private final NameBasedGenerator generator = Generators.nameBasedGenerator();
    private final SecureRandom random = new SecureRandom();

    @Override
    public UUID generate() {
      final byte[] name = new byte[8];
      random.nextBytes(name);
      return generator.generate(name);
    }

    @Override
    public UUID generate(final String name) {
      return generator.generate(name);
    }
  }

  static class RandomIdentityGenerator implements IdentityGenerator {

    @Override
    public UUID generate() {
      return UUID.randomUUID();
    }

    @Override
    public UUID generate(final String name) {
      return UUID.randomUUID();
    }
  }

  static class TimeBasedIdentityGenerator implements IdentityGenerator {
    private final TimeBasedGenerator generator = Generators.timeBasedGenerator();

    @Override
    public UUID generate() {
      return generator.generate();
    }

    @Override
    public UUID generate(final String name) {
      return generator.generate();
    }
  }
}
