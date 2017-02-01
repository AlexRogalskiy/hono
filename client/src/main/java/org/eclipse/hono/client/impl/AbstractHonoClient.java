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
 */

package org.eclipse.hono.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

/**
 * A base class for implementing Hono API clients.
 * <p>
 * Holds a sender and a receiver to an AMQP 1.0 server and provides
 * support for closing these links gracefully.
 * </p>
 */
public abstract class AbstractHonoClient {

    protected static final Logger     LOG = LoggerFactory.getLogger(AbstractHonoClient.class);
    protected static final int        DEFAULT_RECEIVER_CREDITS    = 20;

    protected ProtonSender            sender;
    protected ProtonReceiver          receiver;
    protected Context                 context;

    protected AbstractHonoClient(final Context context) {
        this.context = Objects.requireNonNull(context);
    }

    /**
     * Closes this client's sender and receiver links to Hono.
     * 
     * @param closeHandler the handler to be notified about the outcome.
     * @throws NullPointerException if the given handler is {@code null}.
     */
    protected void closeLinks(final Handler<AsyncResult<Void>> closeHandler) {

        Objects.requireNonNull(closeHandler);

        final Future<ProtonSender> senderCloseHandler = Future.future();
        final Future<ProtonReceiver> receiverCloseHandler = Future.future();
        receiverCloseHandler.setHandler(closeAttempt -> {
            if (closeAttempt.succeeded()) {
                closeHandler.handle(Future.succeededFuture());
            } else {
                closeHandler.handle(Future.failedFuture(closeAttempt.cause()));
            }
        });

        senderCloseHandler.compose(closedSender -> {
            if (receiver != null && receiver.isOpen()) {
                receiver.closeHandler(closeAttempt -> {
                    LOG.debug("closed message consumer for [{}]", receiver.getSource().getAddress());
                    receiverCloseHandler.complete(receiver);
                }).close();
            } else {
                receiverCloseHandler.complete();
            }
        }, receiverCloseHandler);

        context.runOnContext(close -> {

            if (sender != null && sender.isOpen()) {
                sender.closeHandler(closeAttempt -> {
                    LOG.debug("closed message sender for [{}]", sender.getTarget().getAddress());
                    senderCloseHandler.complete(sender);
                }).close();
            } else {
                senderCloseHandler.complete();
            }
        });
    }
}
