// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import io.vlingo.actors.Address;
import io.vlingo.actors.AddressFactory;
import io.vlingo.common.identity.IdentityGeneratorType;

public class AddressTest {
  @Test
  public void testNameGiven() throws Exception {
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);

    final Address address = addressFactory.uniqueWith("test-address");
    
    assertNotNull(address);
    assertNotNull(address.idString());
    assertEquals("test-address", address.name());
    
    final Address another = addressFactory.uniqueWith("another-address");
    
    assertNotEquals(another, address);
    assertNotEquals(0, address.compareTo(another));
    assertEquals(address.idTyped().hashCode(), address.hashCode());
  }

  @Test
  public void testNameAndUUIDIdGiven() throws Exception {
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);

    final String id1 = UUID.randomUUID().toString();
    
    final Address address = addressFactory.from(id1, "test-address");
    
    assertNotNull(address);
    assertEquals(id1, address.idTyped().toString());
    assertEquals("test-address", address.name());
    
    final String id2 = UUID.randomUUID().toString();
    
    final Address another = addressFactory.from(id2, "test-address");
    
    assertNotEquals(another, address);
    assertNotEquals(0, address.compareTo(another));
    assertEquals(address, addressFactory.from(id1, "test-address"));
    assertEquals(0, address.compareTo(addressFactory.from(id1, "test-address")));
    assertEquals(UUID.fromString(id1).hashCode(), address.hashCode());
  }

  @Test
  public void testNameAndLongIdGiven() throws Exception {
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.RANDOM);

    final long id = 123;
    
    final Address address = addressFactory.from(id, "test-address");
    
    assertNotNull(address);
    assertEquals(123, address.id());
    assertEquals("test-address", address.name());
    
    final Address another = addressFactory.from(456, "test-address");
    
    assertNotEquals(another, address);
    assertNotEquals(0, address.compareTo(another));
    assertEquals(address, addressFactory.from(id, "test-address"));
    assertEquals(0, address.compareTo(addressFactory.from(id, "test-address")));
  }

  @Test
  public void testTimeBasedOrdering() throws Exception {
    final AddressFactory addressFactory = new GridAddressFactory(IdentityGeneratorType.TIME_BASED);

    final Address[] ordered = new Address[10];
    final Address[] reversed = new Address[10];
    for (int idx = 0; idx < ordered.length; ++idx) {
      ordered[idx] = addressFactory.unique();
      reversed[reversed.length - idx - 1] = ordered[idx];
    }
    Arrays.sort(reversed);
    assertArrayEquals(ordered, reversed);
    Arrays.sort(ordered);
    assertArrayEquals(reversed, ordered);
  }
}
