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
import org.apache.http.HttpHost;
import org.eclipse.hono.service.tenant.CompleteBaseTenantService;
import org.eclipse.hono.util.TenantResult;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import javax.security.auth.x500.X500Principal;
import java.net.HttpURLConnection;

/**
 *
 */
public class ESTenantService extends CompleteBaseTenantService<ESTenantsConfigProperties> {

    RestHighLevelClient client;

    @Override
    public void setConfig(final ESTenantsConfigProperties configuration) {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9299, "http")));
    }

    @Override
    public void add(final String tenantId, final JsonObject tenantObj, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        final IndexRequest request = new IndexRequest("tenants", "doc", tenantId)
                .source(tenantObj, XContentType.JSON)
                .opType(DocWriteRequest.OpType.CREATE);;
        client.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(final IndexResponse indexResponse) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(indexResponse.status().getStatus())));
            }

            @Override
            public void onFailure(final Exception e) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CONFLICT)));
            }
        });
    }

    @Override
    public void update(final String tenantId, final JsonObject tenantObj, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        super.update(tenantId, tenantObj, resultHandler);
    }

    @Override
    public void remove(final String tenantId, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        super.remove(tenantId, resultHandler);
    }

    @Override
    public void get(final String tenantId, final Span span, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        final GetRequest request = new GetRequest("tenants", "doc", tenantId);
        client.getAsync(request, RequestOptions.DEFAULT, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse documentFields) {
                resultHandler.handle(Future.succeededFuture(
                        TenantResult.from(HttpURLConnection.HTTP_OK,
                                JsonObject.mapFrom(documentFields.getSourceAsString()))));
            }

            @Override
            public void onFailure(final Exception e) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            }
        });

        super.get(tenantId, span, resultHandler);
    }

    @Override
    public void get(final X500Principal subjectDn, final Span span, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        super.get(subjectDn, span, resultHandler);
    }

    @Override
    public void stop() throws Exception {
        client.close();
    }
}
