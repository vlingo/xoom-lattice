package io.vlingo.lattice.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WeakQueueTest {


  // Test sample generation and utilities

  private Collection<Sample> generateSamples(final int size) {
    return IntStream.range(0, size)
        .mapToObj(i -> new Sample(i, new Object()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private <T> Collection<T> fMap(Collection<Sample> samples, Function<Sample, T> fun) {
    return samples.stream().map(fun)
        .collect(Collectors.toCollection(ArrayList::new));
  }


  // Generic assertion methods

  private <T> void assertFifo(Collection<T> elements, Supplier<T> testFun) {
    elements.forEach(e -> assertEquals(e, testFun.get()));
  }


  // WeakQueue should not permit nulls

  @Test(expected = NullPointerException.class)
  public void testAddPreventsNulls() {
    WeakQueue<Object> queue = new WeakQueue<>();
    queue.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void testOfferPreventsNulls() {
    WeakQueue<Object> queue = new WeakQueue<>();
    queue.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void testAddAllPreventsNulls() {
    WeakQueue<Object> queue = new WeakQueue<>();
    List<Object> addAll = new ArrayList<>(2);
    addAll.add(new Object());
    addAll.add(null);
    queue.addAll(addAll);
  }


  // WeakQueue should never throw on enqueue of non-null item

  @Test
  public void testAddNeverThrowsOnNonNullArgument() {
    WeakQueue<Object> queue = new WeakQueue<>();
    generateSamples(1000).forEach(queue::add);
  }

  @Test
  public void testOfferNeverThrowsOnNonNullArgument() {
    WeakQueue<Object> queue = new WeakQueue<>();
    generateSamples(1000).forEach(queue::offer);
  }

  @Test
  public void testAddAllNeverThrowsOnNonNullArgument() {
    WeakQueue<Object> queue = new WeakQueue<>();
    queue.addAll(generateSamples(1000));
  }


  // WeakQueue should always present elements in FIFO order

  @Test
  public void testPollPresentsElementsInFIFOOrder() {
    WeakQueue<Object> queue = new WeakQueue<>();
    Collection<Object> samples = fMap(generateSamples(1000), s -> s.object);
    queue.addAll(samples);
    assertFifo(samples, queue::poll);
  }

  @Test
  public void testRemovePresentsElementsInFIFOOrder() {
    WeakQueue<Object> queue = new WeakQueue<>();
    Collection<Object> samples = generateSamples(1000).stream()
        .map(s -> s.object)
        .collect(Collectors.toCollection(ArrayList::new));
    queue.addAll(samples);
    assertFifo(samples, queue::remove);
  }


  // WeakQueue should obey the Queue API protocol

  @Test
  public void testPollReturnsNullOnEmptyQueue() {
    WeakQueue<Object> queue = new WeakQueue<>();
    assertNull(queue.poll());
  }

  @Test
  public void testPollRetrievesAndRemoves() {
    WeakQueue<Object> queue = new WeakQueue<>();
    Object object = new Object();
    queue.offer(object);
    assertEquals(object, queue.poll());
    assertNull(queue.poll());
  }

  @Test(expected = NoSuchElementException.class)
  public void testRemoveThrowsOnEmptyQueue() {
    WeakQueue<Object> queue = new WeakQueue<>();
    queue.remove();
  }

  @Test
  public void testRemoveRetrievesAndRemoves() {
    WeakQueue<Object> queue = new WeakQueue<>();
    Object object = new Object();
    queue.offer(object);
    assertEquals(object, queue.remove());
    assertNull(queue.poll());
  }

  @Test
  public void testPeekReturnsNullOnEmptyQueue() {
    WeakQueue<Object> queue = new WeakQueue<>();
    assertNull(queue.peek());
  }

  @Test
  public void testPeekRetrievesButDoesNotRemove() {
    WeakQueue<Object> queue = new WeakQueue<>();
    final Object object = new Object();
    queue.offer(object);
    assertEquals(object, queue.peek());
    assertEquals(object, queue.peek());
  }

  @Test(expected = NoSuchElementException.class)
  public void testElementThrowsOnEmptyQueue() {
    WeakQueue<Object> queue = new WeakQueue<>();
    queue.element();
  }

  @Test
  public void testElementRetrievesButDoesNotRemove() {
    WeakQueue<Object> queue = new WeakQueue<>();
    final Object object = new Object();
    queue.offer(object);
    assertEquals(object, queue.element());
    assertEquals(object, queue.element());
  }


  // WeakQueue should not present Garbage Collected elements

  @Test
  public void testPollExpungesGCedElementsPreservingFIFOOrder() {
    WeakQueue<Object> queue = new WeakQueue<>();

    Collection<Sample> samples = generateSamples(1000);

    // keep hard references to only half of the samples
    Collection<Object> hard = samples.stream()
        .filter(p -> p.index % 2 == 0)
        .map(p -> p.object)
        .collect(Collectors.toCollection(ArrayList::new));

    samples.forEach(p -> queue.add(p.object));

    // dereference samples
    samples = null;

    // force GC
    System.gc();

    assertFifo(hard, queue::poll);
  }


  // WeakQueue should obey the Collection API protocol TODO


  // WeakQueue should be thread-safe TODO



  // Internal helper classes

  private static class Sample {
    final int index;
    final Object object;

    private Sample(int index, Object object) {
      this.index = index;
      this.object = object;
    }
  }

}
