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
import org.eclipse.hono.service.registration.CompleteBaseRegistrationService;
import org.eclipse.hono.service.tenant.CompleteBaseTenantService;
import org.eclipse.hono.util.RegistrationResult;
import org.eclipse.hono.util.TenantObject;
import org.eclipse.hono.util.TenantResult;
import org.infinispan.Cache;

import javax.security.auth.x500.X500Principal;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.Optional;

public class CacheRegistrationService extends CompleteBaseRegistrationService<CacheRegistrationConfigProperties> {

    Cache<RegistrationKey, JsonObject> registrationCache;

    @Override
    public void setConfig(CacheRegistrationConfigProperties configuration) {

    }

    protected CacheRegistrationService(Cache<RegistrationKey, JsonObject> cache) {
        this.registrationCache = cache;
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

    @Override
    public void assertRegistration(String tenantId, String deviceId, Span span, Handler<AsyncResult<RegistrationResult>> resultHandler) {
        handleUnimplementedOperation(resultHandler);
    }

    @Override
    public void assertRegistration(String tenantId, String deviceId, String gatewayId, Span span, Handler<AsyncResult<RegistrationResult>> resultHandler) {
        handleUnimplementedOperation(resultHandler);
    }
}
