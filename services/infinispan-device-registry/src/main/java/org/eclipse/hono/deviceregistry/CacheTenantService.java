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
import org.eclipse.hono.util.TenantObject;
import org.eclipse.hono.util.TenantResult;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.security.auth.x500.X500Principal;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "hono.app.type", havingValue = "infinispan", matchIfMissing = true)
public class CacheTenantService extends CompleteBaseTenantService<CacheTenantConfigProperties> {

    Cache<String, RegistryTenantObject> tenantsCache;

    @Override
    public void setConfig(CacheTenantConfigProperties configuration) {

    }

    @Autowired
    protected CacheTenantService(EmbeddedCacheManager cacheManager) {
        this.tenantsCache = cacheManager.createCache("tenants", new ConfigurationBuilder().build());
    }

    @Override
    public void add(String tenantId, JsonObject tenantObj, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        final TenantObject tenantDetails = Optional.ofNullable(tenantObj)
                .map(json -> json.mapTo(TenantObject.class)).orElse(null);

        //verify if a duplicate cert exists
        if (tenantDetails.getTrustedCaSubjectDn() != null){
            RegistryTenantObject tenant = searchByCert(tenantDetails.getTrustedCaSubjectDn().getName());
            if (tenant != null) resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CONFLICT)));
        } else {
            tenantsCache.putIfAbsentAsync(tenantId, new RegistryTenantObject(tenantDetails)).thenAccept(result -> {
                if (result == null) {
                    resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CREATED)));
                } else {
                    resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CONFLICT)));
                }
            });
        }
    }

    @Override
    public void update(String tenantId, JsonObject tenantObj, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        final TenantObject tenantDetails = Optional.ofNullable(tenantObj)
                .map(json -> json.mapTo(TenantObject.class)).orElse(null);

        //verify if a duplicate cert exists
        if (tenantDetails.getTrustedCaSubjectDn() != null){
            RegistryTenantObject tenant = searchByCert(tenantDetails.getTrustedCaSubjectDn().getName());
             if (tenant != null) resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CONFLICT)));
        } else {
            tenantsCache.replaceAsync(tenantId, new RegistryTenantObject(tenantDetails)).thenAccept(result -> {
                        resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
                    }
            );
        }
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

        tenantsCache.getAsync(tenantId).thenAccept(registryTenantObject -> {
            if (registryTenantObject == null) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                resultHandler.handle(Future.succeededFuture(
                        TenantResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(registryTenantObject.getTenantObject()))));
            }
        });
    }

    @Override
    public void get(X500Principal subjectDn, Span span, Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {

        RegistryTenantObject searchResult = searchByCert(subjectDn.getName());

        if (searchResult == null) {
            resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
        } else {
            TenantResult.from(HttpURLConnection.HTTP_OK, JsonObject.mapFrom(searchResult.getTenantObject()));
        }
    }

    // TODO : search by certificate async ?
    private RegistryTenantObject searchByCert(String subjectDnName){

        System.out.println("Getting tenant with X500 SubjectDn : "+subjectDnName);

        QueryFactory queryFactory = Search.getQueryFactory(tenantsCache);
        Query query = queryFactory
                .from(RegistryTenantObject.class)
                .having("trustedCa")
                .contains(subjectDnName).build();

        List<RegistryTenantObject> matches = query.list();

        // TODO make a difference between not found and conflict?
        if (matches.size() != 1){
            return null;
        }else {
            return matches.get(0);
        }
    }
}
