package io.vlingo.xoom.lattice.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WeakQueue<T> implements Queue<T> {

  private final AtomicBoolean idle;

  private Queue<WeakReference<T>> delegate;


  public WeakQueue() {
    this(new LinkedList<>());
  }

  private WeakQueue(Queue<WeakReference<T>> delegate) {
    this.idle = new AtomicBoolean(true);
    this.delegate = delegate;
  }


  private Queue<WeakReference<T>> getDelegate() {
    expungeStaleEntries();
    return delegate;
  }

  private void expungeStaleEntries() {
    delegate.removeIf(weak ->
        Objects.isNull(weak.get()));
  }

  private <A> A atomic(Supplier<A> supplier) {
    try {
      while (idle.compareAndSet(true, false)) ;
      return supplier.get();
    } finally {
      idle.set(true);
    }
  }
  
  private T expungeStaleEntryOnSupply(Supplier<WeakReference<T>> supplier) {
    return Optional.ofNullable(supplier.get())
        .map(w ->
            Optional.ofNullable(w.get())
                .orElseGet(() ->
                    expungeStaleEntryOnSupply(supplier)))
        .orElse(null);
  }

  // Queue methods

  @Override
  public boolean add(T t) {
    Objects.requireNonNull(t, "Null entries not allowed");
    return atomic(() -> getDelegate().add(
        new WeakReference<>(t)));
  }

  @Override
  public boolean offer(T t) {
    Objects.requireNonNull(t, "Null entries not allowed");
    return atomic(() -> getDelegate().offer(
        new WeakReference<>(t)));
  }

  @Override
  public T remove() {
    return atomic(() ->
        expungeStaleEntryOnSupply(getDelegate()::remove));
  }

  @Override
  public T poll() {
    return atomic(() ->
        expungeStaleEntryOnSupply(delegate::poll));
  }

  @Override
  public T element() {
    return atomic(() ->
        expungeStaleEntryOnSupply(delegate::element));
  }

  @Override
  public T peek() {
    return atomic(() ->
        expungeStaleEntryOnSupply(getDelegate()::peek));
  }

  // Collection methods

  @Override
  public int size() {
    return atomic(() -> getDelegate().size());
  }

  @Override
  public boolean isEmpty() {
    return atomic(() -> getDelegate().isEmpty());
  }

  @Override
  public boolean contains(Object o) {
    return atomic(() -> getDelegate().stream()
        .map(WeakReference::get)
        .anyMatch(e -> Objects.nonNull(e) && e.equals(o)));
  }

  @Override
  public Iterator<T> iterator() {
    return new ExpungingIterator(delegate.iterator());
  }

  @Override
  public Object[] toArray() {
    return atomic(() -> getDelegate().stream()
        .map(WeakReference::get)
        .filter(Objects::nonNull)
        .toArray());
  }

  @Override
  public <A> A[] toArray(A[] a) {
    return atomic(() -> getDelegate().stream()
        .map(WeakReference::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new))
        .toArray(a));
  }

  @Override
  public boolean remove(Object o) {
    return atomic(() -> {
      this.delegate = getDelegate().stream()
          .filter(w -> !o.equals(w.get()))
          .collect(Collectors.toCollection(LinkedList::new));
      return delegate.remove(o);
    });
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // TODO unwrap
    return delegate.containsAll(c);
  }

  public boolean addAll(Collection<? extends T> c) {
    c.forEach(Objects::requireNonNull);
    return atomic(() -> getDelegate().addAll(c.stream()
        .map(t -> new WeakReference<T>(t))
        .collect(Collectors.toCollection(ArrayList::new))
    ));
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    // TODO unwrap
    return delegate.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO unwrap
    return delegate.retainAll(c);
  }

  @Override
  public void clear() {
    // TODO sync
    delegate.clear();
  }

  @Override
  public boolean equals(Object o) {
    // TODO sync? identity?
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    // TODO sync? identity?
    return delegate.hashCode();
  }

  // Iterator

  private class ExpungingIterator implements Iterator<T> {

    private final Iterator<WeakReference<T>> iterator;

    private T next;

    private ExpungingIterator(Iterator<WeakReference<T>> iterator) {
      this.iterator = iterator;
    }

    private WeakReference<T> iterate() {
      if (iterator.hasNext()) {
        return iterator.next();
      }
      else {
        return null;
      }
    }

    @Override
    public boolean hasNext() {
      next = expungeStaleEntryOnSupply(this::iterate);
      return next != null;
    }

    @Override
    public T next() {
      return next;
    }

    @Override
    public void remove() {
      iterator.remove();
    }
  }
}
