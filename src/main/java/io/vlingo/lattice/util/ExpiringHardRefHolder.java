package io.vlingo.lattice.util;

import io.vlingo.actors.Actor;
import io.vlingo.common.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Supplier;

/**
 * Holds hard references in a queue and provides means to expunge "expired" references based on timeout duration.
 */
public class ExpiringHardRefHolder extends Actor implements HardRefHolder, Scheduled<Object> {

  private static final Logger logger =
      LoggerFactory.getLogger(ExpiringHardRefHolder.class);


  private final Supplier<Instant> now;
  private final Duration timeout;
  private final PriorityQueue<Expiring> queue;


  public ExpiringHardRefHolder() {
    this(Duration.ofSeconds(20));
  }

  public ExpiringHardRefHolder(final Duration timeout) {
    this(timeout, 1000);
  }

  public ExpiringHardRefHolder(final Duration timeout,
                               final int initialCapacity) {
    this(Instant::now, timeout, initialCapacity);
  }

  ExpiringHardRefHolder(final Supplier<Instant> now,
                        final Duration timeout,
                        final int initialCapacity) {
    this.now = now;
    this.timeout = timeout;
    this.queue = new PriorityQueue<>(initialCapacity);

    scheduler().schedule(selfAs(Scheduled.class), null, 0, 1000);
  }


  @Override
  public void holdOnTo(Object object) {
    final Instant expiry = now.get()
        .plus(timeout);
    logger.debug("Holding on to {} until {}", object, expiry);
    queue.offer(new Expiring(expiry, object));
  }

  @Override
  public void intervalSignal(Scheduled<Object> scheduled, Object data) {
    logger.debug("Starting expired references cleanup at {} ...", now.get());
    int count = 0;
    Expiring next;
    do {
      next = queue.peek();
      if (next == null) {
        // queue is empty
        break;
      }
      if (next.bestBefore.isBefore(now.get())) {
        // dereference
        queue.remove();
        ++count;
      }
      else {
        // stop
        next = null;
      }
    } while (next != null);
    logger.debug("Finished cleanup of expired references at {}. {} removed.",
        now.get(), count);
  }

  /**
   * Note: this class has a natural ordering that is inconsistent with equals.
   */
  private static class Expiring implements Comparable<Expiring> {
    final Instant bestBefore;
    final Object reference;


    private Expiring(Instant bestBefore, Object reference) {
      Objects.requireNonNull(bestBefore);
      this.bestBefore = bestBefore;
      Objects.requireNonNull(reference);
      this.reference = reference;
    }

    @Override
    public int compareTo(Expiring o) {
      if (Objects.isNull(o)) {
        return 1;
      }
      return this.bestBefore.compareTo(o.bestBefore);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Expiring expiring = (Expiring) o;
      return bestBefore.equals(expiring.bestBefore) &&
          reference.equals(expiring.reference);
    }

    @Override
    public int hashCode() {
      return Objects.hash(bestBefore, reference);
    }

    @Override
    public String toString() {
      return "Expiring(" +
          "bestBefore=" + bestBefore +
          ", reference=" + reference +
          ')';
    }
  }

}
