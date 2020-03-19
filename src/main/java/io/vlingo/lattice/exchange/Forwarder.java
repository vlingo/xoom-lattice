// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import java.util.ArrayList;
import java.util.List;

/**
 * Forwarder of all local and exchange messages. Registers Covey instances
 * through which forwarding is accomplished.
 */
public class Forwarder {
  private final List<Covey<?, ?, ?>> coveys;

  /**
   * Constructs this Forwarder.
   */
  public Forwarder() {
    this.coveys = new ArrayList<>();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <L,EX> void forwardToReceiver(final EX exchangeMessage) {
    final Covey covey = ofExchangeMessage(exchangeMessage);
    final Object localObject = covey.adapter.fromExchange(covey.exchangeClass.cast(exchangeMessage));
    covey.receiver.receive(covey.localClass.cast(localObject));
  }

  /**
   * Forward the {@code L} local object as an {@code EX} exchange message to the sender.
   * @param local the {@code L} local object
   * @param <L> the local object type
   * @param <EX> the exchange message type
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <L,EX> void forwardToSender(final L local) {
    final Covey covey = ofObjectType(local.getClass());
    final EX exchangeTypedMessage = (EX) covey.adapter.toExchange(covey.localClass.cast(local));
    covey.sender.send(exchangeTypedMessage);
  }

  public <L,E,EX> void register(final Covey<L,E,EX> covey) {
    coveys.add(covey);
  }

  /**
   * Answer the {@code Covey<?,?,?>} of the {@code exchangeMessage}.
   * @param exchangeMessage the Object to match
   * @return {@code Covey<?,?,?>}
   */
  private Covey<?,?,?> ofExchangeMessage(final Object exchangeMessage) {
    for (final Covey<?,?,?> covey : coveys) {
      if (covey.adapter.supports(exchangeMessage)) {
        return covey;
      }
    }
    throw new IllegalArgumentException("Not a supported message type: " + exchangeMessage.getClass().getName());
  }

  /**
   * Answer the {@code Covey<?,?,?>} of the {@code objectType}.
   * @param objectType the {@code Class<?>} to match
   * @return {@code Covey<?,?,?>}
   */
  private Covey<?,?,?> ofObjectType(final Class<?> objectType) {
    for (final Covey<?,?,?> covey : coveys) {
      if (covey.externalClass == objectType || covey.localClass == objectType) {
        return covey;
      }
    }
    throw new IllegalArgumentException("Not a supported object type: " + objectType.getName());
  }
}
