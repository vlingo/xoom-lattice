// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.streams;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.reactivestreams.Sink;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Temporary added here from vlingo-streams project.
 * @param <T>
 */
class SafeConsumerSink<T> implements Sink<T>, Consumer<T> {
    private AccessSafely access = afterCompleting(0);

    private AtomicInteger readyCount = new AtomicInteger(0);
    private AtomicInteger terminateCount = new AtomicInteger(0);
    private AtomicInteger valueCount = new AtomicInteger(0);

    private final List<T> values = new CopyOnWriteArrayList<>();

    @Override
    public void accept(final T value) {
        whenValue(value);
    }

    @Override
    public void ready() {
        access.writeUsing("ready", 1);
    }

    @Override
    public void terminate() {
        access.writeUsing("terminate", 1);
    }

    @Override
    public void whenValue(final T value) {
        access.writeUsing("value", 1);
        access.writeUsing("values", value);
    }

    public AccessSafely afterCompleting(final int times) {
        access = AccessSafely.afterCompleting(times);

        access.writingWith("ready", (Integer value) -> readyCount.addAndGet(value));
        access.writingWith("terminate", (Integer value) -> { terminateCount.addAndGet(value); });
        access.writingWith("value", (Integer value) -> valueCount.addAndGet(value));

        access.writingWith("values", (T value) -> values.add(value));

        access.readingWith("ready", () -> readyCount.get());
        access.readingWith("terminate", () -> terminateCount.get());
        access.readingWith("value", () -> valueCount.get());

        access.readingWith("values", () -> values);

        return access;
    }

    public int accessValueMustBe(final String name, final int expected) {
        int current = 0;
        for (int tries = 0; tries < 10; ++tries) {
            final int value = access.readFrom(name);
            if (value >= expected) {
                return value;
            }
            if (current != value) {
                current = value;
                // System.out.println("VALUE: " + value);
            }
            try { Thread.sleep(100); } catch (Exception e) { }
        }
        return expected == 0 ? -1 : current;
    }

    @Override
    public String toString() {
        return "SafeConsumerSink";
    }
}
