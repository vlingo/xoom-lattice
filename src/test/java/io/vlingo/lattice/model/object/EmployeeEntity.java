// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.object;

import io.vlingo.common.Completes;

public class EmployeeEntity extends ObjectEntity<EmployeeState> implements Employee {
  private EmployeeState employee;

  public EmployeeEntity() {
    this.employee = new EmployeeState(); // unidentified
  }

  public EmployeeEntity(final long id) {
    this.employee = new EmployeeState(id, "", 0); // recover
  }

  @Override
  public Completes<EmployeeState> current() {
    return completes().with(employee);
  }

  @Override
  public Completes<EmployeeState> assign(final String number) {
    return apply(employee.with(number), new EmployeeNumberAssigned(), () -> employee);
  }

  @Override
  public Completes<EmployeeState> adjust(final int salary) {
    return apply(employee.with(salary), new EmployeeSalaryAdjusted(), () -> employee);
  }

  @Override
  public Completes<EmployeeState> hire(final String number, final int salary) {
    return apply(employee.with(number).with(salary), new EmployeeHired(), () -> employee);
  }

  @Override
  protected String id() {
    return String.valueOf(employee.persistenceId());
  }

  @Override
  protected EmployeeState stateObject() {
    return employee;
  }

  @Override
  protected void stateObject(final EmployeeState stateObject) {
    this.employee = stateObject;
  }

  @Override
  protected Class<EmployeeState> stateObjectType() {
    return EmployeeState.class;
  }
}
