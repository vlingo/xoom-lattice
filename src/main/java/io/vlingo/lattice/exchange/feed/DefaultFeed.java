package io.vlingo.lattice.exchange.feed;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Stage;
import io.vlingo.symbio.store.EntryReader;

/**
 * The default implementation of {@code Feed}. See the default behaviors
 * in {@code Feed} for specific defaults.
 */
public class DefaultFeed implements Feed {
  private final EntryReader<?> entryReaderType;
  private final String exchangeName;
  private final Class<? extends Actor> feederType;
  private final Stage stage;

  /**
   * @see io.vlingo.lattice.exchange.feed.Feed#entryReaderType()
   */
  @Override
  public EntryReader<?> entryReaderType() {
    return entryReaderType;
  }

  /*
   * @see io.vlingo.lattice.exchange.feed.Feed#feederType()
   */
  @Override
  public Class<? extends Actor> feederType() {
    return feederType;
  }

  /**
   * @see io.vlingo.lattice.exchange.feed.Feed#feeder()
   */
  @Override
  public Feeder feeder() {
    return stage.actorFor(Feeder.class, feederType(), this, entryReaderType());
  }

  /**
   * @see io.vlingo.lattice.exchange.feed.Feed#exchangeName()
   */
  @Override
  public String exchangeName() {
    return exchangeName;
  }

  /**
   * Construct my default state.
   * @param stage this Stage of actors I create
   * @param exchangeName the String name of the exchange I feed
   * @param feederType the Class<? extends Actor> of my Feeder
   * @param entryReaderType the EntryReader<?> used by my Feeder
   */
  DefaultFeed(final Stage stage, final String exchangeName, final Class<? extends Actor> feederType, final EntryReader<?> entryReaderType) {
    this.stage = stage;
    this.exchangeName = exchangeName;
    this.feederType = feederType;
    this.entryReaderType = entryReaderType;
  }
}
