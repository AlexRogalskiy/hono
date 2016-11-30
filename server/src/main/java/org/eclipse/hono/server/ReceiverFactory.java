/**
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial creation
 *
 */

package org.eclipse.hono.server;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonQoS;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

/**
 * A factory for creating {@code ProtonSender} instances.
 */
public interface ReceiverFactory {

    /**
     * Creates a new sender from a connection and a target address.
     * <p>
     * The sender created will be open and ready to use.
     * 
     * @param connection The connection to create the sender from.
     * @param address The target address to use for the sender.
     * @param qos The quality of service the sender should use.
//     * @param sendQueueDrainHandler The handler to notify about credits the sender is getting replenished with.
     * @param result The future to notify about the outcome of the creation.
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    void createReceiver(
            ProtonConnection connection,
            String address,
            ProtonQoS qos,
//            Handler<ProtonReceiver> sendQueueDrainHandler,
            Future<ProtonReceiver> result);
}
