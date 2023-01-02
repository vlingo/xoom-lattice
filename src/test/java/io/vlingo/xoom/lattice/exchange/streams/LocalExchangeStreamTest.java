// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.exchange.streams;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Protocols;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.message.AsyncMessageQueue;
import io.vlingo.xoom.common.message.MessageQueue;
import io.vlingo.xoom.lattice.exchange.Covey;
import io.vlingo.xoom.lattice.exchange.Exchange;
import io.vlingo.xoom.lattice.exchange.ExchangeReceiver;
import io.vlingo.xoom.lattice.exchange.ExternalType1;
import io.vlingo.xoom.lattice.exchange.LocalType1;
import io.vlingo.xoom.lattice.exchange.local.LocalExchange;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeAdapter;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeMessage;
import io.vlingo.xoom.lattice.exchange.local.LocalExchangeSender;
import io.vlingo.xoom.reactivestreams.PublisherConfiguration;
import io.vlingo.xoom.reactivestreams.StreamSubscriber;
import io.vlingo.xoom.reactivestreams.Streams;
import io.vlingo.xoom.reactivestreams.sink.test.SafeConsumerSink;

public class LocalExchangeStreamTest {
    private World world;
    private Publisher<LocalType1> publisher;
    private ExchangeReceiver<LocalType1> receiver;

    final LocalType1 local1 = new LocalType1("ABC", 123);

    @Before
    public void setUp() {
        world = World.startWithDefaults("streams");
    }

    @After
    public void tearDown() {
        world.terminate();
    }

    private void createProxyWith(final ExchangeStreamSource<LocalType1> source) {
        final Definition definition = Definition.has(ExchangeStreamPublisher.class, Definition.parameters(source,
                new PublisherConfiguration(5, Streams.OverflowPolicy.DropHead)));
        final Protocols protocols = world.actorFor(new Class[] { Publisher.class, ExchangeReceiver.class }, definition);
        publisher = protocols.get(0);
        receiver = protocols.get(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExchangeStreams() {
        final MessageQueue queue = new AsyncMessageQueue(null);
        createProxyWith(new ExchangeStreamSource<>(false));
        final Exchange exchange = new LocalExchange(queue);

        exchange
                .register(Covey.of(
                        new LocalExchangeSender(queue),
                        receiver,
                        new LocalExchangeAdapter<LocalType1, ExternalType1>(LocalType1.class),
                        LocalType1.class,
                        ExternalType1.class,
                        LocalExchangeMessage.class));

        // subscribe to ExchangeStreamProxy with standard Subscriber and consume the message with a Sink
        SafeConsumerSink<LocalType1> sink = new SafeConsumerSink<>();
        Subscriber<LocalType1> subscriber = world.actorFor(Subscriber.class, StreamSubscriber.class, sink, 2);
        final AccessSafely access = sink.afterCompleting(2);
        publisher.subscribe(subscriber);

        exchange.send(local1);

        final List<LocalType1> values = access.readFrom("values");
        assertEquals(1, values.size());
        assertEquals(local1.attribute1, values.get(0).attribute1);
        assertEquals(local1.attribute2, values.get(0).attribute2);

        exchange.close();
    }
}
