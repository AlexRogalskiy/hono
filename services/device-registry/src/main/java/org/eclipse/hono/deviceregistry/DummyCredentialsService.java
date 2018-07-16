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
import org.eclipse.hono.service.credentials.BaseCredentialsService;
import org.eclipse.hono.util.CacheDirective;
import org.eclipse.hono.util.CredentialsObject;
import org.eclipse.hono.util.CredentialsResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;

/**
 *
 */
@Service
@Primary
public final class DummyCredentialsService extends BaseCredentialsService<Object> {

    @Override
    public void setConfig(final Object configuration) {

    }

    @Override
    public void get(final String tenantId, final String type, final String authId, final Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {
        get(tenantId, type, authId, null, resultHandler);
    }

    @Override
    public void get(final String tenantId, final String type, final String authId, final JsonObject clientContext, final Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {
        final JsonObject result = JsonObject.mapFrom(CredentialsObject.fromHashedPassword(authId, authId,
                "hono-secret", "sha-256", null, null, null));
        resultHandler.handle(Future.succeededFuture(
                CredentialsResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(result), CacheDirective.noCacheDirective())));
    }

}
