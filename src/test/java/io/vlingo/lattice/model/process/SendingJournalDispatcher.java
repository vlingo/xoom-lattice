// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.process;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.lattice.exchange.Exchange;
import io.vlingo.symbio.BaseEntry.TextEntry;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.dispatch.Dispatchable;
import io.vlingo.symbio.store.dispatch.Dispatcher;
import io.vlingo.symbio.store.dispatch.DispatcherControl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SendingJournalDispatcher implements Dispatcher<Dispatchable<Entry<String>, State<?>>> {

  private AccessSafely access;
  private final ProcessMessageTextAdapter adapter;
  private final Exchange exchange;

  private List<Entry<String>> entries = new CopyOnWriteArrayList<>();

  public SendingJournalDispatcher(final Exchange exchange, final ProcessMessageTextAdapter adapter) {
    super();
    this.exchange = exchange;
    this.adapter = adapter;
    this.access = afterCompleting(0);
  }

  @Override
  public void controlWith(final DispatcherControl control) {

  }

  @Override
  public void dispatch(final Dispatchable<Entry<String>, State<?>> dispatchable) {
    access.writeUsing("appendedAll", dispatchable.entries());
    sendEntryMessages(dispatchable.entries());
  }

  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely.afterCompleting(times)

            .writingWith("appended", (Entry<String> appended) -> entries.add(appended))
            .writingWith("appendedAll", (List<Entry<String>> appended) -> entries.addAll(appended))
            .readingWith("appendedAt", (Integer index) -> entries.get(index))

            .readingWith("entries", () -> entries)
            .readingWith("entriesCount", () -> entries.size());

    return access;
  }

  private void sendEntryMessage(final Entry<String> entry) {
    final ProcessMessage message = adapter.fromEntry((TextEntry) entry);
    exchange.send(message.source);
  }

  private void sendEntryMessages(List<Entry<String>> entries) {
    for (final Entry<String> entry : entries) {
      sendEntryMessage(entry);
    }
  }

}
