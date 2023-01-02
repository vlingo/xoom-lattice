// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.object;

import io.vlingo.xoom.common.Completes;

public class EmployeeEntity extends ObjectEntity<EmployeeState> implements Employee {
  private EmployeeState employee;

  public EmployeeEntity(final String id) {
    super(id);
    this.employee = new EmployeeState(Long.parseLong(id), id, 0);
  }

  @Override
  public Completes<EmployeeState> current() {
    return completes().with(employee);
  }

  @Override
  public Completes<EmployeeState> adjust(final int salary) {
    return apply(employee.with(salary), new EmployeeSalaryAdjusted(), () -> employee);
  }

  @Override
  public Completes<EmployeeState> hire(final int salary) {
    return apply(employee.with(salary), new EmployeeHired(), () -> employee);
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
