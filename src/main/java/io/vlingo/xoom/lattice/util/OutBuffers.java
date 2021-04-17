package io.vlingo.xoom.lattice.util;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import io.vlingo.xoom.wire.node.Id;

public final class OutBuffers {

  private static Queue<Runnable> EMPTY = new LinkedList<>();


  private final Supplier<Queue<Runnable>> queueInitializer;
  private final Map<Id, Queue<Runnable>> buffers;

  private final HardRefHolder holder;

  public OutBuffers(final HardRefHolder holder) {
    this(WeakQueue::new, holder);
  }

  public OutBuffers(final Supplier<Queue<Runnable>> queueInitializer,
                    final HardRefHolder holder) {
    this(queueInitializer, holder, 5, .85F);
  }

  public OutBuffers(final Supplier<Queue<Runnable>> queueInitializer,
                    final HardRefHolder holder,
                    final int size,
                    final float loadFactor) {
    this.queueInitializer = queueInitializer;
    this.holder = holder;
    this.buffers = new ConcurrentHashMap<>(size, loadFactor);
  }


  public void enqueue(final Id id, final Runnable task) {
    if (!buffers.containsKey(id)) {
      buffers.putIfAbsent(id, queueInitializer.get());
    }
    if (Objects.nonNull(holder)) {
      holder.holdOnTo(task);
    }
    buffers.get(id).offer(task);
  }

  public Queue<Runnable> queue(final Id id) {
    return buffers.getOrDefault(id, EMPTY);
  }
}
