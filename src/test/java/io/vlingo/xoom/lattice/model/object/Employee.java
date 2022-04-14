// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.object;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.TestEvents.Event;

public interface Employee {
  Completes<EmployeeState> current();
  Completes<EmployeeState> adjust(final int salary);
  Completes<EmployeeState> hire(final int salary);


  public static final class EmployeeHired extends Event { }
  public static final class EmployeeSalaryAdjusted extends Event { }
}
