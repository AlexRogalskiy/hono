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
import org.eclipse.hono.service.tenant.CompleteBaseTenantService;
import org.eclipse.hono.util.TenantConstants;
import org.eclipse.hono.util.TenantObject;
import org.eclipse.hono.util.TenantResult;
import org.infinispan.Cache;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import javax.security.auth.x500.X500Principal;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CacheTenantService extends CompleteBaseTenantService<CacheTenantConfigProperties> {

    Cache<String, TenantObject> tenantsCache;

    @Override
    public void setConfig(CacheTenantConfigProperties configuration) {

    }

    protected CacheTenantService(Cache<String, TenantObject> cache) {
        this.tenantsCache = cache;
    }

    @Override
    public void add(String tenantId, JsonObject tenantObj, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        // TODO : handle duplicate CA

        final TenantObject tenantDetails = Optional.ofNullable(tenantObj)
                .map(json -> json.mapTo(TenantObject.class)).orElse(null);

        tenantsCache.putIfAbsentAsync(tenantId, tenantDetails).thenAccept(result -> {
            if ( result == null){
                    resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CREATED)));
            } else {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CONFLICT)));
            }
        });
    }

    @Override
    public void update(String tenantId, JsonObject tenantObj, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        // TODO : handle duplicate CA

        final TenantObject tenantDetails = Optional.ofNullable(tenantObj)
                .map(json -> json.mapTo(TenantObject.class)).orElse(null);

        tenantsCache.replaceAsync(tenantId, tenantDetails).thenAccept(result -> {
                    resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
                }
        );
    }

    @Override
    public void remove(String tenantId, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        tenantsCache.removeAsync(tenantId).thenAccept(result -> {
                    resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
                }
        );
    }

    @Override
    public void get(String tenantId, Span span, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(resultHandler);

        tenantsCache.getAsync(tenantId).thenAccept(tenantDetails -> {
            if (tenantDetails == null) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                resultHandler.handle(Future.succeededFuture(
                        TenantResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(tenantDetails))));
            }
        });
    }

    @Override
    // TODO : async
    public void get(X500Principal subjectDn, Span span, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        handleUnimplementedOperation(resultHandler);
        /*
        System.out.println("Getting tenant with X500 SubjectDn");

        QueryFactory queryFactory = Search.getQueryFactory(tenantsCache);
        Query query = queryFactory.from(TenantObject.class).having(TenantConstants.FIELD_PAYLOAD_SUBJECT_DN).eq(subjectDn.getName()).build();

        // Execute the query
        List<RegistryCredentialObject> matches = query.list();

        if (matches.size() != 1 ){
            resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
        }else {
            matches.forEach(tenant -> {
                resultHandler.handle(Future.succeededFuture(
                        TenantResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(tenant))));
            });
        }
        */
    }
}
