// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange;

/**
 * A set of {@code Exchange} components.
 *
 * @param <L> the local object type
 * @param <E> the external object type
 * @param <EX> the exchange message type
 */
public class Covey<L,E,EX> {
  public ExchangeAdapter<L,E,EX> adapter;
  public ExchangeReceiver<L> receiver;
  public ExchangeSender<EX> sender;
  public final Class<L> localClass;
  public final Class<E> externalClass;
  public final Class<EX> exchangeClass;

  /**
   * Answer the {@code Covey<L,E,EX>} information from a set of related components, which includes
   * {@code ExchangeSender<EX>}, {@code ExchangeReceiver<L>}, {@code ExchangeAdapter<L,E,EX>}, and the classes of the local and
   * channel types. These will be used to send and receive a specific kind of messages and to adapt such from/to
   * channel and local types.
   * @param sender the {@code ExchangeSender<EX>} through which a local message type is sent but first adapted to a channel message
   * @param receiver the {@code ExchangeReceiver<L>} through which messages are received as local types
   * @param adapter the {@code ExchangeAdapter<L,E,EX>} that adapts/maps local messages to channel and channel messages to local
   * @param localClass the {@code Class<L>} of the local object type
   * @param externalClass the {@code Class<E>} of the external object type
   * @param exchangeClass the {@code Class<EX} of the exchange message type
   * @param <L> the local object type
   * @param <E> the external object type
   * @param <EX> the exchange message type
   * @return {@code Covey<L,E,EX>}
   */
  public static <L,E,EX> Covey<L,E,EX> of(final ExchangeSender<EX> sender, final ExchangeReceiver<L> receiver, final ExchangeAdapter<L,E,EX> adapter, final Class<L> localClass, final Class<E> externalClass, final Class<EX> exchangeClass) {
    return new Covey<L,E,EX>(sender, receiver, adapter, localClass, externalClass, exchangeClass);
  }

  /**
   * Constructs the covey information from a set of related components, which includes {@code ExchangeSender<EX>},
   * {@code ExchangeReceiver<L>}, {@code ExchangeAdapter<L,E,EX>}, and the classes of the local and channel types.
   * These will be used to send and receive a specific kind of messages and to adapt such from/to channel and local types.
   * @param sender the {@code ExchangeSender<EX>} through which a local message type is sent but first adapted to an exchange message
   * @param receiver the {@code ExchangeReceiver<L>} through which messages are received as local object types
   * @param adapter the {@code ExchangeAdapter<L,E,EX>} that adapts/maps local objects to exchange messages and exchange messages to local objects
   * @param localClass the {@code Class<L>} of the local object type
   * @param externalClass the {@code Class<E>} of the external object type
   * @param exchangeClass the {@code Class<EX} of the exchange message type
   */
  public Covey(final ExchangeSender<EX> sender, final ExchangeReceiver<L> receiver, final ExchangeAdapter<L,E,EX> adapter, final Class<L> localClass, final Class<E> externalClass, final Class<EX> exchangeClass) {
    assert(sender != null);
    this.sender = sender;
    assert(receiver != null);
    this.receiver = receiver;
    assert(adapter != null);
    this.adapter = adapter;
    assert(localClass != null);
    this.localClass = localClass;
    assert(externalClass != null);
    this.externalClass = externalClass;
    assert(exchangeClass != null);
    this.exchangeClass = exchangeClass;
  }
}
