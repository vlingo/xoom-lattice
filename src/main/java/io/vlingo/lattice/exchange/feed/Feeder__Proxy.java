// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.feed;

import io.vlingo.actors.Actor;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.common.SerializableConsumer;

public class Feeder__Proxy implements io.vlingo.lattice.exchange.feed.Feeder {

  private static final String feedItemToRepresentation1 = "feedItemTo(io.vlingo.lattice.exchange.feed.FeedItemId, io.vlingo.lattice.exchange.feed.FeedConsumer)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Feeder__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void feedItemTo(io.vlingo.lattice.exchange.feed.FeedItemId arg0, io.vlingo.lattice.exchange.feed.FeedConsumer arg1) {
    if (!actor.isStopped()) {
      final SerializableConsumer<Feeder> consumer = (actor) -> actor.feedItemTo(arg0, arg1);
      if (mailbox.isPreallocated()) { mailbox.send(actor, Feeder.class, consumer, null, feedItemToRepresentation1); }
      else { mailbox.send(new LocalMessage<Feeder>(actor, Feeder.class, consumer, feedItemToRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, feedItemToRepresentation1));
    }
  }
}
