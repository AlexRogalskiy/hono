/*******************************************************************************
 * Copyright (c) 2016, 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.hono.deviceregistry;

import io.opentracing.Span;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.eclipse.hono.auth.HonoPasswordEncoder;
import org.eclipse.hono.service.credentials.CompleteBaseCredentialsService;
import org.eclipse.hono.util.CacheDirective;
import org.eclipse.hono.util.CredentialsConstants;
import org.eclipse.hono.util.CredentialsObject;
import org.eclipse.hono.util.CredentialsResult;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheCredentialService extends CompleteBaseCredentialsService<CacheCredentialConfigProperties> {

    Cache<CredentialsKey, CredentialsObject> credentialsCache;

    /**
     * Creates a new service instance for a password encoder.
     *
     * @param pwdEncoder The encoder to use for hashing clear text passwords.
     * @throws NullPointerException if encoder is {@code null}.
     */
    protected CacheCredentialService(Cache<CredentialsKey, CredentialsObject> credentialsCache, HonoPasswordEncoder pwdEncoder) {
        super(pwdEncoder);
        this.credentialsCache = credentialsCache;
    }

    @Override
    public void setConfig(CacheCredentialConfigProperties configuration) {

    }

    @Override
    public void add(String tenantId, JsonObject credentialsJson, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        final CredentialsObject credentials = Optional.ofNullable(credentialsJson)
                .map(json -> json.mapTo(CredentialsObject.class)).orElse(null);

        CredentialsKey key = new CredentialsKey(tenantId, credentials.getAuthId(), credentials.getType());

        credentialsCache.putAsync(key, credentials).thenAccept(result -> {
                    resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_CREATED)));
                }
        );
    }

    /**
     * {@inheritDoc}
     * <p>
     * The result object will include a <em>no-cache</em> directive.
     */
    @Override
    public void get(final String tenantId, final String type, final String authId, final Span span,
                    final Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {
        get(tenantId, type, authId, null, span, resultHandler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The result object will include a <em>no-cache</em> directive.
     */
    @Override
    public void get(
            final String tenantId,
            final String type,
            final String authId,
            final JsonObject clientContext,
            final Span span,
            final Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(authId);
        Objects.requireNonNull(resultHandler);

        CredentialsKey key = new CredentialsKey(tenantId, authId, type);

        credentialsCache.getAsync(key).thenAccept(credentials -> {
            if (credentials == null) {
                resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                //TODO handle client context
                resultHandler.handle(Future.succeededFuture(
                        CredentialsResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(credentials), CacheDirective.noCacheDirective())));
            }
        });

    }

    @Override
    public void update(String tenantId, JsonObject otherKeys, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {
        super.update(tenantId, otherKeys, resultHandler);
    }

    @Override
    public void remove(String tenantId, String type, String authId, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {
        super.remove(tenantId, type, authId, resultHandler);
    }

    @Override
    public void removeAll(String tenantId, String deviceId, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {
        super.removeAll(tenantId, deviceId, resultHandler);
    }

    @Override
    public void getAll(String tenantId, String deviceId, Span span, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

    }
}
