// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduled;

public class MultiConfirmingProjectionControlActor extends Actor implements MultiConfirming, ProjectionControl, Scheduled<Object> {
  private final long expiration;
  private final Map<String, Confirmable> confirmables;
  private final ProjectionControl projectionControl;

  public MultiConfirmingProjectionControlActor(final ProjectionControl projectionControl, final long expiration) {
    this.projectionControl = projectionControl;
    this.confirmables = new HashMap<>();
    this.expiration = expiration;
  }

  //==========================
  // MultiConfirming
  //==========================

  @Override
  public void manageConfirmationsFor(final Projectable projectable, final int count) {
    final long expiresBy = new Date().getTime() + expiration;

    confirmables.put(projectable.projectionId(), new Confirmable(projectable, count, 0, expiresBy));
  }

  //==========================
  // ProjectionControl
  //==========================

  @Override
  public Completes<List<Projectable>> managedConfirmations() {
    final List<Projectable> managedConfirmations =
            confirmables
              .values()
              .stream()
              .map(confirmable -> confirmable.projectable)
              .collect(Collectors.toList());

    return completes().with(Collections.unmodifiableList(managedConfirmations));
  }

  @Override
  public void confirmProjected(final String projectionId) {
    final Confirmable confirmable = confirmables.get(projectionId);

    if (confirmable == null) return; // too many confirms possible

    final int total = confirmable.total + 1;

    if (confirmable.count < total) {
      confirmables.put(projectionId, confirmable.incrementTotal());
    } else {
      ProjectionControl.confirmerFor(confirmable.projectable, projectionControl).confirm();
      confirmables.remove(projectionId);
    }
  }

  //==========================
  // Scheduled
  //==========================

  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    final long currentTime = new Date().getTime();
    final List<String> expiredKeys = new ArrayList<>();

    for (final String projectionId : confirmables.keySet()) {
      final Confirmable confirmable = confirmables.get(projectionId);
      if (confirmable.expiresBy <= currentTime) {
        expiredKeys.add(projectionId);
      }
    }

    for (final String projectionId : expiredKeys) {
      logger().info("Removing expired confirmable: " + projectionId);
      confirmables.remove(projectionId);
    }
  }

  private class Confirmable {
    final Projectable projectable;
    final int count;
    final int total;
    final long expiresBy;

    public Confirmable(final Projectable projectable, final int count, final int total, final long expiresBy) {
      this.projectable = projectable;
      this.count = count;
      this.total = total;
      this.expiresBy = expiresBy;
    }

    public Confirmable incrementTotal() {
      return new Confirmable(projectable, count, total + 1, expiresBy);
    }
  }
}
