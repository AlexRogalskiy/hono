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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.hono.auth.HonoPasswordEncoder;
import org.eclipse.hono.service.credentials.CompleteBaseCredentialsService;
import org.eclipse.hono.util.CacheDirective;
import org.eclipse.hono.util.CredentialsConstants;
import org.eclipse.hono.util.CredentialsObject;
import org.eclipse.hono.util.CredentialsResult;
import org.infinispan.Cache;

import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheCredentialService extends CompleteBaseCredentialsService<CacheCredentialConfigProperties> {

    Cache<CredentialsKey, RegistryCredentialObject> credentialsCache;

    /**
     * Creates a new service instance for a password encoder.
     *
     * @param pwdEncoder The encoder to use for hashing clear text passwords.
     * @throws NullPointerException if encoder is {@code null}.
     */
    protected CacheCredentialService(Cache<CredentialsKey, RegistryCredentialObject> credentialsCache, HonoPasswordEncoder pwdEncoder) {
        super(pwdEncoder);
        this.credentialsCache = credentialsCache;
    }

    @Override
    public void setConfig(CacheCredentialConfigProperties configuration) {

    }

    @Override
    public void add(String tenantId, JsonObject credentialsJson, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        System.out.println("ADD : "+credentialsJson);
        final CredentialsObject credentials = Optional.ofNullable(credentialsJson)
                .map(json -> json.mapTo(CredentialsObject.class)).orElse(null);

        CredentialsKey key = new CredentialsKey(tenantId, credentials.getAuthId(), credentials.getType());
        RegistryCredentialObject registryCredential = new RegistryCredentialObject(credentials, tenantId, credentialsJson);

        credentialsCache.putIfAbsentAsync(key, registryCredential).thenAccept(result -> {
            if ( result == null){
                    resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_CREATED)));
            } else {
                resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_CONFLICT)));
            }
        });
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

        System.out.println("GET context : "+clientContext);


        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(authId);
        Objects.requireNonNull(resultHandler);

        CredentialsKey key = new CredentialsKey(tenantId, authId, type);
        System.out.println("searching with "+tenantId+ authId+ type);

       credentialsCache.getAsync(key).thenAccept(credential -> {
            if (credential == null) {
               resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else if (clientContext != null) {
                if ( contextMatches(clientContext, credential.getOriginalJson()) ) {
                    resultHandler.handle(Future.succeededFuture(
                            CredentialsResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(credential.getOriginalJson()), CacheDirective.noCacheDirective())));
                } else{
                    resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
                }
            } else {
                resultHandler.handle(Future.succeededFuture(
                        CredentialsResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(credential.getOriginalJson()), CacheDirective.noCacheDirective())));
            }
        });
    }

    @Override
    public void update(String tenantId, JsonObject otherKeys, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        final CredentialsObject credentials = Optional.ofNullable(otherKeys)
                .map(json -> json.mapTo(CredentialsObject.class)).orElse(null);

        CredentialsKey key = new CredentialsKey(tenantId, credentials.getAuthId(), credentials.getType());
        RegistryCredentialObject registryCredential = new RegistryCredentialObject(credentials, tenantId, otherKeys);

        credentialsCache.replaceAsync(key, registryCredential).thenAccept(result -> {
                    resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
                }
        );
    }

    @Override
    public void remove(String tenantId, String type, String authId, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        CredentialsKey key = new CredentialsKey(tenantId, authId, type);
        credentialsCache.removeAsync(key).thenAccept(result -> {
                    resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
                }
        );
    }

    @Override
    public void removeAll(String tenantId, String deviceId, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        System.out.println("removing by device "+deviceId);

        List<RegistryCredentialObject>  matches = queryAllCredentialsForDevice(tenantId, deviceId);
        matches.forEach(registryCredential -> {
            CredentialsKey key = new CredentialsKey(
                    tenantId,
                    registryCredential.getOriginalJson().getString(CredentialsConstants.FIELD_AUTH_ID),
                    registryCredential.getOriginalJson().getString(CredentialsConstants.FIELD_TYPE));
            credentialsCache.remove(key);
            System.out.printf("Removed: %s", registryCredential.getDeviceId() +" "+registryCredential.getOriginalJson().encode());
        });
        resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
    }

    @Override
    public void getAll(String tenantId, String deviceId, Span span, Handler<AsyncResult<CredentialsResult<JsonObject>>> resultHandler) {

        JsonArray creds = new JsonArray();

        queryAllCredentialsForDevice(tenantId, deviceId).forEach( result ->{
            creds.add(JsonObject.mapFrom(result.getOriginalJson()));
        });

        if (creds.isEmpty()) {
            resultHandler.handle(Future.succeededFuture(CredentialsResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
        } else {

            final JsonObject result = new JsonObject()
                    .put(CredentialsConstants.FIELD_CREDENTIALS_TOTAL, creds.size())
                    .put(CredentialsConstants.CREDENTIALS_ENDPOINT, creds);
            resultHandler.handle(Future.succeededFuture(
                    CredentialsResult.from(HttpURLConnection.HTTP_OK, result, CacheDirective.noCacheDirective())));
        }
    }

    private List<RegistryCredentialObject> queryAllCredentialsForDevice(String tenantId, String deviceId){
        // Obtain a query factory for the cache
        QueryFactory queryFactory = Search.getQueryFactory(credentialsCache);
        // TODO : async request ?
        Query query = queryFactory.from(RegistryCredentialObject.class)
                .having("deviceId").eq(deviceId)
                .and().having("tenantId").eq(tenantId)
                .build();

        // Execute the query
        return query.list();
    }

    private boolean contextMatches(JsonObject clientContext, JsonObject storedCredential) {
        final AtomicBoolean match = new AtomicBoolean(true);
        clientContext.forEach(field -> {
            if (storedCredential.containsKey(field.getKey())) {
                if (!storedCredential.getString(field.getKey()).equals(field.getValue())) {
                    match.set(false);
                }
            } else {
                match.set(false);
            }
        });
        return match.get();
    }
}
