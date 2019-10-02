package io.vlingo.lattice.exchange;

public class ExchangeException extends RuntimeException {
  /** My serialVersionUID property. */
  private static final long serialVersionUID = 1L;

  /** My retry indicator. */
  private final boolean retry;

  /**
   * Constructs my default state.
   * @param message the String message
   * @param cause the Throwable cause
   * @param retry the boolean indicating whether or not to retry sending
   */
  public ExchangeException(final String message, final Throwable cause, final boolean retry) {
    super(message, cause);
    this.retry = retry;
  }

  /**
   * Constructs my default state.
   * @param message the String message
   * @param cause the Throwable cause
   */
  public ExchangeException(final String message, final Throwable cause) {
    this(message, cause, false);
  }

  /**
   * Constructs my default state.
   * @param message the String message
   * @param retry the boolean indicating whether or not to retry sending
   */
  public ExchangeException(final String message, final boolean retry) {
    super(message);
    this.retry = retry;
  }

  /**
   * Constructs my default state.
   * @param message the String message
   */
  public ExchangeException(final String message) {
    super(message);
    this.retry = false;
  }

  /**
   * Answers whether or not retry is set. Retry can be used by a MessageListener
   * when it wants the message it has attempted to handle to be re-queued rather
   * than rejected, so that it can re-attempt handling later.
   * @return boolean
   */
  public boolean retry() {
    return this.retry;
  }
}
