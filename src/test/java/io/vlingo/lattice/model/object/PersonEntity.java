// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import io.vlingo.common.Completes;

public class PersonEntity extends ObjectEntity<PersonState> implements Person {
  private PersonState person;

  public PersonEntity() {
    this.person = new PersonState(); // unidentified
  }

  public PersonEntity(final long id) {
    this.person = new PersonState(id, "", 0); // recover
  }

  @Override
  public Completes<PersonState> current() {
    return completes().with(person);
  }

  @Override
  public Completes<PersonState> identify(final String name, final int age) {
    apply(new PersonState(name, age), () -> person);
    return completes();
  }

  @Override
  public Completes<PersonState> change(String name) {
    apply(person.with(name), () -> person);
    return completes();
  }

  @Override
  public Completes<PersonState> increaseAge() {
    apply(person.with(person.age + 1), () -> person);
    return completes();
  }

  @Override
  protected String id() {
    return String.valueOf(person.persistenceId());
  }

  @Override
  protected void persistentObject(final PersonState persistentObject) {
    this.person = persistentObject;
  }

  @Override
  protected Class<PersonState> persistentObjectType() {
    return PersonState.class;
  }
}
