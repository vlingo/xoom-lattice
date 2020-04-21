// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.grid.application;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Returns;
import io.vlingo.common.SerializableConsumer;
import io.vlingo.lattice.grid.application.message.Answer;
import io.vlingo.lattice.grid.application.message.Deliver;
import io.vlingo.lattice.grid.application.message.Encoder;
import io.vlingo.lattice.grid.application.message.Forward;
import io.vlingo.lattice.grid.application.message.Message;
import io.vlingo.lattice.grid.application.message.Relocate;
import io.vlingo.lattice.grid.application.message.Start;
import io.vlingo.lattice.util.OutBuffers;
import io.vlingo.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.wire.message.RawMessage;
import io.vlingo.wire.node.Id;

public class OutboundGridActorControl implements GridActorControl.Outbound {

  private static final Logger logger = LoggerFactory.getLogger(OutboundGridActorControl.class);

  private final Id localNodeId;
  private ApplicationOutboundStream stream;
  private final Encoder encoder;
  private final BiConsumer<UUID, Returns<?>> correlation;

  private final OutBuffers outBuffers;


  public OutboundGridActorControl(Id localNodeId, Encoder encoder, BiConsumer<UUID, Returns<?>> correlation, OutBuffers outBuffers) {
    this(localNodeId, null, encoder, correlation, outBuffers);
  }

  public OutboundGridActorControl(Id localNodeId,
                                  ApplicationOutboundStream stream,
                                  Encoder encoder,
                                  BiConsumer<UUID, Returns<?>> correlation,
                                  OutBuffers outBuffers) {
    this.localNodeId = localNodeId;
    this.stream = stream;
    this.encoder = encoder;
    this.correlation = correlation;
    this.outBuffers = outBuffers;
  }

  public void setStream(ApplicationOutboundStream outbound) {
    this.stream = outbound;
  }

  @Override
  public void disburse(Id id) {
    Queue<Runnable> buffer = outBuffers.queue(id);
    logger.debug("Disbursing buffered messages");
    Runnable next;
    do {
      next = buffer.poll();
      if (next != null) {
        next.run();
      }
    } while (next != null);
  }

  private void send(Id recipient, Message message) {
    logger.debug("Buffering message {} to {}", message, recipient);
    outBuffers.enqueue(recipient, () -> {
      logger.debug("Sending message {} to {}", message, recipient);
      byte[] payload = encoder.encode(message);
      RawMessage raw = RawMessage.from(
          localNodeId.value(), -1, payload.length);
      raw.putRemaining(ByteBuffer.wrap(payload));
      stream.sendTo(raw, recipient);
    });
  }

  @Override
  public <T> void start(Id recipient, Id sender, Class<T> protocol, Address address, Definition.SerializationProxy definitionProxy) {
    send(recipient, new Start<>(protocol, address, definitionProxy));
  }

  @Override
  public <T> void deliver(Id recipient, Id sender, Returns<?> returns, Class<T> protocol, Address address, Definition.SerializationProxy definitionProxy, SerializableConsumer<T> consumer, String representation) {
    final Deliver<T> deliver;
    if (returns == null) {
      deliver = new Deliver<>(protocol, address, definitionProxy, consumer, representation);
    } else {
      final UUID answerCorrelationId = UUID.randomUUID();
      deliver = new Deliver<>(protocol, address, definitionProxy, consumer, answerCorrelationId, representation);
      correlation.accept(answerCorrelationId, returns);
    }
    send(recipient, deliver);
  }

  @Override
  public <T> void answer(Id receiver, Id sender, Answer<T> answer) {
    send(receiver, answer);
  }

  @Override
  public void forward(Id receiver, Id sender, Message message) {
    send(receiver, new Forward(sender, message));
  }

  @Override
  public void relocate(Id receiver, Id sender, Definition.SerializationProxy definitionProxy, Address address, Object snapshot, List<? extends io.vlingo.actors.Message> pending) {
    List<Deliver<?>> messages = pending.stream()
        .map(Deliver.from(correlation))
        .collect(Collectors.toList());
    send(receiver, new Relocate(address, definitionProxy, snapshot, messages));
  }
}
