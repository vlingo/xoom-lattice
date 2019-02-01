// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.lattice.exchange;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder and set of metadata attributes, any number of which are common
 * to associate with messages as parameters. Use and ignore as appropriate.
 * The chosen parameters are built using the fluent interface.
 */
public class MessageParameters {
  public static enum DeliveryMode { Durable, Transient }
  public static enum Priority { High, Normal, Medium, Low, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9 }

  /** Identity specific to the application or service. */
  private String applicationId;

  /** Encoding used for this message, defaulting to UTF_8. */
  private String contentEncoding;

  /** Type of content of the message, defaulting to \"text/plain\". */
  private String contentType;

  /** Identity used to correlate with other messages. */
  private String correlationId;

  /** Delivery identity. */
  private String deliveryId;

  /** Delivery mode, either Durable or Transient, defaulting to Transient. */
  private DeliveryMode deliveryMode;

  /** Name of the exchange. */
  private String exchangeName;

  /** Key-value headers to attach to the messages. */
  private Map<String,Object> headers;

  /** Unique identity specific to the message. */
  private String messageId;

  /** Extra parameter 1. */
  private String other1;

  /** Extra parameter 2. */
  private String other2;

  /** Extra parameter 3. */
  private String other3;

  /** Priority of the message, defaulting to Normal. */
  private Priority priority;

  /** Name of the queue. */
  private String queueName;

  /** Re-delivery indicator. */
  private boolean redeliver;

  /** Identification for the receiver to reply to the sender. */
  private String replyTo;

  /** Return address for the receiver to reply to the sender. */
  private String returnAddress;

  /** Routing information. */
  private List<String> routing;

  /** Time that the message was created, defaulting to the current time. */
  private long timestamp;

  /** Time that the message is valid for delivery, defaulting to {@code Long.MAX_VALUE}. */
  private long timeToLive;

  /** Tag metadata. */
  private String tag;

  /** Message type code. */
  private String typeCode;

  /** Message type name. */
  private String typeName;

  /** Identity of the user sending the message. */
  private String userId;

  /**
   * Answer a new MessageParameters with no preset values.
   * @return MessageParameters
   */
  public static MessageParameters bare() {
    return new MessageParameters();
  }

  /**
   * Answer a new MessageParameters with preset defaults.
   * @return MessageParameters
   */
  public static MessageParameters withDefaults() {
    return new MessageParameters()
            .contentEncoding(StandardCharsets.UTF_8.name())
            .contentType("text/plain")
            .deliveryMode(DeliveryMode.Transient)
            .headers(new HashMap<>(0))
            .priority(Priority.Normal)
            .timestamp(System.currentTimeMillis())
            .timeToLive(Long.MAX_VALUE);
  }

  public MessageParameters() { }

  public MessageParameters applicationId(final String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  public String applicationId() {
    return applicationId;
  }

  public MessageParameters contentEncoding(final String contentEncoding) {
    this.contentEncoding = contentEncoding;
    return this;
  }

  public String contentEncoding() {
    return contentEncoding;
  }

  public MessageParameters contentType(final String contentType) {
    this.contentType = contentType;
    return this;
  }

  public String contentType() {
    return contentType;
  }

  public MessageParameters correlationId(final String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  public String correlationId() {
    return correlationId;
  }

  public MessageParameters deliveryId(final String deliveryId) {
    this.deliveryId = deliveryId;
    return this;
  }

  public String deliveryId() {
    return deliveryId;
  }

  public MessageParameters deliveryMode(final DeliveryMode deliveryMode) {
    this.deliveryMode = deliveryMode;
    return this;
  }

  public DeliveryMode deliveryMode() {
    return deliveryMode;
  }

  public boolean isDurableDeliveryMode() {
    return (deliveryMode != null && deliveryMode == DeliveryMode.Durable);
  }

  public boolean isTransientDeliveryMode() {
    return (deliveryMode == null || deliveryMode == DeliveryMode.Transient);
  }

  public MessageParameters exchangeName(final String exchangeName) {
    this.exchangeName = exchangeName;
    return this;
  }

  public String exchangeName() {
    return exchangeName;
  }

  public MessageParameters headers(final Map<String,Object> headers) {
    this.headers = headers;
    return this;
  }

  public Map<String,Object> headers() {
    return headers;
  }

  public MessageParameters messageId(final String messageId) {
    this.messageId = messageId;
    return this;
  }

  public String messageId() {
    return messageId;
  }

  public MessageParameters other1(final String other1) {
    this.other1 = other1;
    return this;
  }

  public String other1() {
    return other1;
  }

  public MessageParameters other2(final String other2) {
    this.other2 = other2;
    return this;
  }

  public String other2() {
    return other2;
  }

  public MessageParameters other3(final String other3) {
    this.other3 = other3;
    return this;
  }

  public String other3() {
    return other3;
  }

  public MessageParameters priority(final Priority priority) {
    this.priority = priority;
    return this;
  }

  public Priority priority() {
    return priority;
  }

  public MessageParameters queueName(final String queueName) {
    this.queueName = queueName;
    return this;
  }

  public String queueName() {
    return queueName;
  }

  public MessageParameters redeliver(final boolean redeliver) {
    this.redeliver = redeliver;
    return this;
  }

  public boolean redeliver() {
    return redeliver;
  }

  public MessageParameters replyTo(final String replyTo) {
    this.replyTo = replyTo;
    return this;
  }

  public String replyTo() {
    return replyTo;
  }

  public MessageParameters returnAddress(final String returnAddress) {
    this.returnAddress = returnAddress;
    return this;
  }

  public String returnAddress() {
    return returnAddress;
  }

  public MessageParameters routing(final String...routings) {
    this.routing = Arrays.asList(routings);
    return this;
  }

  public MessageParameters routing(final List<String> routings) {
    this.routing = routings;
    return this;
  }

  public List<String> routing() {
    return routing;
  }

  public MessageParameters tag(final String tag) {
    this.tag = tag;
    return this;
  }

  public String tag() {
    return tag;
  }

  public MessageParameters timestamp(final long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public long timestamp() {
    return timestamp;
  }

  public MessageParameters timeToLive(final long timeToLive) {
    this.timeToLive = timeToLive;
    return this;
  }

  public long timeToLive() {
    return timeToLive;
  }

  public MessageParameters typeCode(final String typeCode) {
    this.typeCode = typeCode;
    return this;
  }

  public String typeCode() {
    return typeCode;
  }

  public MessageParameters typeName(final String typeName) {
    this.typeName = typeName;
    return this;
  }

  public String typeName() {
    return typeName;
  }

  public MessageParameters userId(final String userId) {
    this.userId = userId;
    return this;
  }

  public String userId() {
    return userId;
  }
}
