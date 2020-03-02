// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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
    return apply(new PersonState(name, age), () -> person);
  }

  @Override
  public Completes<PersonState> change(String name) {
    return apply(person.with(name), () -> person);
  }

  @Override
  public Completes<PersonState> increaseAge() {
    return apply(person.with(person.age + 1), () -> person);
  }

  @Override
  protected String id() {
    return String.valueOf(person.persistenceId());
  }

  @Override
  protected PersonState stateObject() {
    return person;
  }

  @Override
  protected void stateObject(final PersonState stateObject) {
    this.person = stateObject;
  }

  @Override
  protected Class<PersonState> stateObjectType() {
    return PersonState.class;
  }

  @Override
  public void applyRelocationSnapshot(String snapshot) {
    stateObject(new PersonState(Long.parseLong(snapshot), null, 0));
  }
}
