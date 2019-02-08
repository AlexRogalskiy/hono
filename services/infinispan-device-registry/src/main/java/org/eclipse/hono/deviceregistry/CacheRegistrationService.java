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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.eclipse.hono.service.registration.CompleteBaseRegistrationService;
import org.eclipse.hono.util.RegistrationResult;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.net.HttpURLConnection;

@Repository
@ConditionalOnProperty(name = "hono.app.type", havingValue = "infinispan", matchIfMissing = true)
public class CacheRegistrationService extends CompleteBaseRegistrationService<CacheRegistrationConfigProperties> {

    Cache<RegistrationKey, JsonObject> registrationCache;

    @Override
    public void setConfig(CacheRegistrationConfigProperties configuration) {

    }
    @Autowired
    protected CacheRegistrationService(EmbeddedCacheManager cacheManager) {
        this.registrationCache = cacheManager.createCache("registration", new ConfigurationBuilder().build());
    }


    @Override
    public void addDevice(String tenantId, String deviceId, JsonObject otherKeys, Handler<AsyncResult<RegistrationResult>> resultHandler) {

        RegistrationKey key = new RegistrationKey(tenantId, deviceId);

        registrationCache.putIfAbsentAsync(key, otherKeys).thenAccept(result -> {
            if ( result == null){
                    resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_CREATED)));
            } else {
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_CONFLICT)));
            }
        });
    }

    @Override
    public void updateDevice(String tenantId, String deviceId, JsonObject otherKeys, Handler<AsyncResult<RegistrationResult>> resultHandler) {

        RegistrationKey key = new RegistrationKey(tenantId, deviceId);
        registrationCache.replaceAsync(key, otherKeys).thenAccept( result -> {
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
        });
    }

    @Override
    public void removeDevice(String tenantId, String deviceId, Handler<AsyncResult<RegistrationResult>> resultHandler) {

        RegistrationKey key = new RegistrationKey(tenantId, deviceId);
        registrationCache.removeAsync(key).thenAccept( result -> {
            if ( result == null){
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
            }
        });
    }

    @Override
    public void getDevice(String tenantId, String deviceId, Handler<AsyncResult<RegistrationResult>> resultHandler) {
        RegistrationKey key = new RegistrationKey(tenantId, deviceId);

        registrationCache.getAsync(key).thenAccept( result -> {
            if ( result == null){
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                resultHandler.handle(Future.succeededFuture(
                        RegistrationResult.from(HttpURLConnection.HTTP_OK, result)));
            }
        });
    }
}
