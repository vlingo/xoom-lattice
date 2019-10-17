// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.spaces;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import io.vlingo.actors.Actor;
import io.vlingo.common.Cancellable;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;

public class SpaceActor extends Actor implements Space, Scheduled<Object> {
  private static final long Brief = 5;
  private static final long Rounding = 100;

  private Optional<Cancellable> cancellable;
  private Duration currentSweepInterval;
  private final Set<ExpirableItem<?>> expirableItems;
  private final Duration defaultSweepInterval;
  private final Map<Class<Key>, Map<Key,ExpirableItem<?>>> registry;
  private final Scheduled<Object> scheduled;

  @SuppressWarnings("unchecked")
  public SpaceActor(final Duration defaultSweepInterval) {
    this.defaultSweepInterval = defaultSweepInterval;
    this.cancellable = Optional.empty();
    this.expirableItems = new TreeSet<>();
    this.currentSweepInterval = Duration.ofMillis(Long.MAX_VALUE);
    this.registry = new HashMap<>();
    this.scheduled = selfAs(Scheduled.class);
  }

  @Override
  public <T> Completes<T> itemFor(final Class<T> protocol, final Class<? extends Actor> type, final Object... parameters) {
    // Fail; not implemented. See SpaceItemFactoryRelay#itemFor.
    return completes().with(null);
  }

  @Override
  public <T> Completes<KeyItem<T>> put(final Key key, final Item<T> item) {
    manage(key, item);

    return completes().with(KeyItem.of(key, item.object, item.lease));
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> get(final Key key, final Period until) {
    final ExpirableItem<T> item = item(key);

    return completes().with(Optional.of(KeyItem.of(key, item.object, item.lease)));
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> take(final Key key, final Period until) {
    final ExpirableItem<T> item = item(key);

    return completes().with(Optional.of(KeyItem.of(key, item.object, item.lease)));
  }

  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    currentSweepInterval = sweep();

    cancellable = Optional.of(scheduler().scheduleOnce(scheduled, null, Duration.ZERO, currentSweepInterval));
  }

  private <T> ExpirableItem<T> expiringItem(final Key key, final Item<T> item) {
    final Instant expiration = item.lease.toFutureInstant();
    return new ExpirableItem<>(key, item.object, expiration, item.lease);
  }

  private <T> ExpirableItem<T> item(final Key key) {
    final Map<Key,ExpirableItem<T>> itemMap = itemMap(key);
    return itemMap.get(key);
  }

  @SuppressWarnings("unchecked")
  private <T> Map<Key, ExpirableItem<T>> itemMap(final Key key) {
    final Class<Key> keyClass = (Class<Key>) key.getClass();

    Map<Key,ExpirableItem<T>> itemMap = getItemMap(keyClass);

    if (itemMap == null) {
      itemMap = new HashMap<>();
      putItemMap(keyClass, itemMap);
    }

    return itemMap;
  }

  @SuppressWarnings("rawtypes")
  private Map getItemMap(final Class<Key> keyClass) {
    return registry.get(keyClass);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <T> void putItemMap(final Class<Key> keyClass, Map itemMap) {
    registry.put(keyClass, itemMap);
  }

  private <T> void manage(final Key key, final Item<T> item) {
    final ExpirableItem<T> expiringItem = expiringItem(key, item);

    final Map<Key,ExpirableItem<T>> itemMap = itemMap(expiringItem.key);

    itemMap.put(expiringItem.key, expiringItem);

    if (!expiringItem.isMaximumExpiration()) {
      expirableItems.add(expiringItem);

      scheduleWith(item);
    }
  }

  private void scheduleWith(final Item<?> leasingItem) {
    final long rounded = leasingItem.lease.duration.toMillis() + Rounding;

    if (rounded < currentSweepInterval.toMillis()) {
      currentSweepInterval = leasingItem.lease.duration;

      cancellable.ifPresent(canceller -> canceller.cancel());

      cancellable = Optional.of(scheduler().scheduleOnce(scheduled, null, Duration.ZERO, currentSweepInterval));
    }
  }

  private Duration sweep() {
    final Instant now = Instant.now();

    final List<ExpirableItem<?>> confirmedExpirables = new ArrayList<>();

    for (final ExpirableItem<?> expirableItem : expirableItems) {
      if (now.isAfter(expirableItem.expiresOn)) {
        if (itemMap(expirableItem.key).remove(expirableItem.key) != null) {
          confirmedExpirables.add(expirableItem);
        }
      }
    }

    for (final ExpirableItem<?> expirableItem : confirmedExpirables) {
      expirableItems.remove(expirableItem);
    }

    final Iterator<ExpirableItem<?>> iterator = expirableItems.iterator();

    if (iterator.hasNext()) {
      final long millis = iterator.next().expiresOn.toEpochMilli() - Instant.now().toEpochMilli();
      return Duration.ofMillis(millis < 0 ? Brief : millis);
    } else {
      return defaultSweepInterval;
    }
  }
}
