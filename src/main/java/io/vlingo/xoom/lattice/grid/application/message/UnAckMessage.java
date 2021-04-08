// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.grid.application.message;

import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.wire.node.Id;

/**
 * This class represents an unacknowledged message which has been sent to recipient.
 */
public class UnAckMessage {
    private final Id receiver;
    private final Returns<?> returns;
    private final Deliver<?> message;

    public UnAckMessage(Id receiver, Returns<?> returns, Deliver<?> message) {
        this.receiver = receiver;
        this.returns = returns;
        this.message = message;
    }

    public Id getReceiver() {
        return receiver;
    }

    @SuppressWarnings("unchecked")
    public Returns<Object> getReturns() {
        return (Returns<Object>) returns;
    }

    public Deliver<?> getMessage() {
        return message;
    }
}
