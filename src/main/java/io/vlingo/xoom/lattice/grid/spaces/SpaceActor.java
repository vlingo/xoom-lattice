// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.spaces;

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

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.common.Cancellable;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduled;

public class SpaceActor extends Actor implements Space, Scheduled<ScheduledScanner<?>> {
  private static final long Brief = 5;
  private static final long Rounding = 100;

  private final Set<ExpirableItem<?>> expirableItems;
  private final Set<ExpirableQuery> expirableQueries;
  private final Duration defaultScanInterval;
  private final Map<Class<Key>, Map<Key,ExpirableItem<?>>> registry;
  private final ScheduledQueryRunnerEvictor scheduledQueryRunnerEvictor;
  private final ScheduledSweeper scheduledSweeper;
  private final Scheduled<ScheduledScanner<?>> scheduled;

  @SuppressWarnings("unchecked")
  public SpaceActor(final Duration defaultScanInterval) {
    this.defaultScanInterval = defaultScanInterval;
    this.expirableItems = new TreeSet<>();
    this.expirableQueries = new TreeSet<>();
    this.registry = new HashMap<>();
    this.scheduled = selfAs(Scheduled.class);
    this.scheduledQueryRunnerEvictor = new ScheduledQueryRunnerEvictor();
    this.scheduledSweeper = new ScheduledSweeper();
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
    final ExpirableItem<T> item = item(key, true);

    if (item == null) {
      periodicQuery(key, true, until);

      return completes();
    } else {
      return completes().with(Optional.of(KeyItem.of(key, item.object, item.lease)));
    }
  }

  @Override
  public <T> Completes<Optional<KeyItem<T>>> take(final Key key, final Period until) {
    final ExpirableItem<T> item = item(key, false);

    if (item == null) {
      periodicQuery(key, false, until);

      return completes();
    } else {
      return completes().with(Optional.of(KeyItem.of(key, item.object, item.lease)));
    }
  }

  @Override
  public void intervalSignal(final Scheduled<ScheduledScanner<?>> scheduled, final ScheduledScanner<?> scanner) {
    scanner.scan();
  }

  private <T> ExpirableItem<T> expiringItem(final Key key, final Item<T> item) {
    final Instant expiration = item.lease.toFutureInstant();
    return new ExpirableItem<>(key, item.object, expiration, item.lease);
  }

  private <T> ExpirableQuery expiringQuery(final Key key, final boolean retainItem, final Period period) {
    final Instant expiration = period.toFutureInstant();
    return new ExpirableQuery(key, retainItem, expiration, period, completesEventually());
  }

  private <T> ExpirableItem<T> item(final Key key, final boolean retain) {
    final Map<Key,ExpirableItem<T>> itemMap = itemMap(key);

    if (retain) {
      return itemMap.get(key);
    } else {
      return itemMap.remove(key);
    }
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

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <T> void manage(final Key key, final Item<T> item) {
    final ExpirableItem<T> expiringItem = expiringItem(key, item);

    final Map<Key,ExpirableItem<T>> itemMap = itemMap(expiringItem.key);

    itemMap.put(expiringItem.key, expiringItem);

    if (!expiringItem.isMaximumExpiration()) {
      expirableItems.add(expiringItem);

      scheduledSweeper.scheduleBy((Item) item);
    }
  }

  private void periodicQuery(final Key key, final boolean retain, final Period until) {
    final ExpirableQuery query = expiringQuery(key, retain, until);

    expirableQueries.add(query);

    scheduledQueryRunnerEvictor.scheduleBy(query);
  }

  //================================
  // ScheduledQueryRunnerEvictor
  //================================

  private class ScheduledQueryRunnerEvictor implements ScheduledScanner<ExpirableQuery> {
    private Optional<Cancellable> cancellable;
    private Duration currentDuration;

    ScheduledQueryRunnerEvictor() {
      this.cancellable = Optional.empty();
      this.currentDuration = Duration.ofMillis(Long.MAX_VALUE);
    }

    @Override
    public void scan() {
      final Instant now = Instant.now();

      final List<ExpirableQuery> confirmedExpirables = new ArrayList<>();

      for (final ExpirableQuery expirableQuery : expirableQueries) {
        final ExpirableItem<?> item = item(expirableQuery.key, expirableQuery.retainItem);

        if (item != null) {
          expirableQuery.completes.with(Optional.of(KeyItem.of(item.key, item.object, item.lease)));
          confirmedExpirables.add(expirableQuery);
        } else {
          if (now.isAfter(expirableQuery.expiresOn)) {
            confirmedExpirables.add(expirableQuery);
            expirableQuery.completes.with(Optional.empty());
          }
        }
      }

      for (final ExpirableQuery expirableQuery : confirmedExpirables) {
        expirableQueries.remove(expirableQuery);
      }

      final Iterator<ExpirableQuery> iterator = expirableQueries.iterator();

      if (iterator.hasNext()) {
        final long millis = (iterator.next().expiresOn.getEpochSecond() - Instant.now().getEpochSecond()) * 1_000;
        final Duration minQueryDuration = Duration.ofMillis(millis < 0 ? Rounding : millis);
        currentDuration = min(minQueryDuration, defaultScanInterval);
      } else {
        currentDuration = defaultScanInterval;
      }

      schedule();
    }

    @Override
    public void scheduleBy(final ScheduledScannable<ExpirableQuery> scannable) {
      final ExpirableQuery query = scannable.scannable();
      final long rounded = query.period.toMilliseconds() + Rounding;

      if (rounded < currentDuration.toMillis()) {
        currentDuration = min(query.period.duration, defaultScanInterval);
      }

      schedule();
    }

    private Duration min(final Duration duration1, final Duration duration2) {
      return duration1.toMillis() < duration2.toMillis() ? duration1 : duration2;
    }

    private void schedule() {
      cancellable.ifPresent(canceller -> canceller.cancel());

      cancellable = Optional.of(scheduler().scheduleOnce(scheduled, this, Duration.ZERO, currentDuration));
    }
  }

  //================================
  // ScheduledSweeper
  //================================

  @SuppressWarnings("rawtypes")
  private class ScheduledSweeper implements ScheduledScanner<Item> {
    private Optional<Cancellable> cancellable;
    private Duration currentDuration;

    ScheduledSweeper() {
      this.cancellable = Optional.empty();
      this.currentDuration = Duration.ofMillis(Long.MAX_VALUE);
    }

    @Override
    public void scan() {
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
        currentDuration = Duration.ofMillis(millis < 0 ? Brief : millis);
      } else {
        currentDuration = defaultScanInterval;
      }

      schedule();
    }

    @Override
    public void scheduleBy(final ScheduledScannable<Item> scannable) {
      final Item item = scannable.scannable();
      final long rounded = item.lease.duration.toMillis() + Rounding;

      if (rounded < currentDuration.toMillis()) {
        currentDuration = item.lease.duration;

        schedule();
      }
    }

    private void schedule() {
      cancellable.ifPresent(canceller -> canceller.cancel());

      cancellable = Optional.of(scheduler().scheduleOnce(scheduled, this, Duration.ZERO, currentDuration));
    }
  }
}
