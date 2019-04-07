// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.symbio.store.object.PersistentObject;

public class EmployeeState extends PersistentObject implements Comparable<EmployeeState> {
  private static final long serialVersionUID = 1L;

  private static final AtomicLong identityGenerator = new AtomicLong(0);

  public final int salary;
  public final String number;

  public EmployeeState(final String number, final int salary) {
    this(identityGenerator.incrementAndGet(), number, salary);
  }

  public EmployeeState(final long id, final String number, final int salary) {
    super(id);
    this.number = number;
    this.salary = salary;
  }

  EmployeeState() {
    super(identityGenerator.incrementAndGet());
    this.number = "";
    this.salary = 0;
  }

  public EmployeeState with(final String number) {
    return new EmployeeState(this.persistenceId(), number, salary);
  }

  public EmployeeState with(final int salary) {
    return new EmployeeState(this.persistenceId(), number, salary);
  }

  @Override
  public int hashCode() {
    return 31 * number.hashCode() * salary;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != getClass()) {
      return false;
    } else if (this == other) {
      return true;
    }

    final EmployeeState otherPerson = (EmployeeState) other;

    return this.persistenceId() == otherPerson.persistenceId();
  }

  @Override
  public String toString() {
    return "EmployeeState[persistenceId=" + persistenceId() + " number=" + number + " salary=" + salary + "]";
  }

  @Override
  public int compareTo(final EmployeeState otherPerson) {
    return Long.compare(this.persistenceId(), otherPerson.persistenceId());
  }
}
