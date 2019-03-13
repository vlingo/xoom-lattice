// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.model.sourcing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.journal.JournalListener;

public final class MockJournalListener implements JournalListener<String> {
  
  private AccessSafely access;

  private List<Entry<String>> entries = new CopyOnWriteArrayList<>();

  public MockJournalListener() {
    super();
    this.access = afterCompleting(0);
  }

  @Override
  public void appended(Entry<String> entry) {
    access.writeUsing("appended", entry);
  }

  @Override
  public void appendedWith(Entry<String> entry, State<String> snapshot) {
    access.writeUsing("appended", entry);
  }

  @Override
  public void appendedAll(List<Entry<String>> entries) {
    access.writeUsing("appendedAll", entries);
  }

  @Override
  public void appendedAllWith(List<Entry<String>> entries, State<String> snapshot) {
    access.writeUsing("appendedAll", entries);
  }
  
  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely
      .afterCompleting(times)
      
      .writingWith("appended", (Entry<String> appended) -> entries.add(appended))
      .writingWith("appendedAll", (List<Entry<String>> appended) -> entries.addAll(appended))
      .readingWith("appendedAt", (Integer index) -> entries.get(index))
      
      .readingWith("entries", () -> entries)
      .readingWith("entriesCount", () -> entries.size());

    return access;
  }
}
