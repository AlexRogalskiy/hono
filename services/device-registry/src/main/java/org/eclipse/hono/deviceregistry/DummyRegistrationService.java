/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial creation
 *******************************************************************************/
package org.eclipse.hono.deviceregistry;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.eclipse.hono.service.registration.BaseRegistrationService;
import org.eclipse.hono.util.RegistrationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;

/**
 *
 */
@Service
@ConditionalOnProperty(name = "hono.app.type", havingValue = "dummy")
public class DummyRegistrationService extends BaseRegistrationService<Object> {

    @Override
    public void setConfig(final Object configuration) {

    }

    @Override
    public void assertRegistration(final String tenantId, final String deviceId, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
        assertRegistration(tenantId, deviceId, null, resultHandler);
    }

    @Override
    public void assertRegistration(final String tenantId, final String deviceId, final String gatewayId, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
       final JsonObject deviceData = new JsonObject();

        resultHandler.handle(Future.succeededFuture(RegistrationResult.from(
                HttpURLConnection.HTTP_OK,
                getAssertionPayload(tenantId, deviceId, deviceData))));
    }
}
