// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange.streams;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Protocols;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.message.AsyncMessageQueue;
import io.vlingo.common.message.MessageQueue;
import io.vlingo.lattice.exchange.*;
import io.vlingo.lattice.exchange.local.LocalExchange;
import io.vlingo.lattice.exchange.local.LocalExchangeAdapter;
import io.vlingo.lattice.exchange.local.LocalExchangeMessage;
import io.vlingo.lattice.exchange.local.LocalExchangeSender;
import io.vlingo.reactivestreams.PublisherConfiguration;
import io.vlingo.reactivestreams.StreamSubscriber;
import io.vlingo.reactivestreams.Streams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
