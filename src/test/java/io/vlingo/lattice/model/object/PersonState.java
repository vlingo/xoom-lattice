// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.symbio.store.object.StateObject;

public class PersonState extends StateObject implements Comparable<PersonState> {
  private static final long serialVersionUID = 1L;

  private static final AtomicLong identityGenerator = new AtomicLong(0);

  public final int age;
  public final String name;

  public PersonState(final String name, final int age) {
    this(identityGenerator.incrementAndGet(), name, age);
  }

  public PersonState(final long id, final String name, final int age) {
    super(id);
    this.name = name;
    this.age = age;
  }

  PersonState() {
    super();
    this.name = "";
    this.age = 0;
  }

  public PersonState with(final String name) {
    return new PersonState(this.persistenceId(), name, age);
  }

  public PersonState with(final int age) {
    return new PersonState(this.persistenceId(), name, age);
  }

  @Override
  public int hashCode() {
    return 31 * name.hashCode() * age;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != getClass()) {
      return false;
    } else if (this == other) {
      return true;
    }

    final PersonState otherPerson = (PersonState) other;

    return this.persistenceId() == otherPerson.persistenceId();
  }

  @Override
  public String toString() {
    return "Person[persistenceId=" + persistenceId() + " name=" + name + " age=" + age + "]";
  }

  @Override
  public int compareTo(final PersonState otherPerson) {
    return Long.compare(this.persistenceId(), otherPerson.persistenceId());
  }
}
