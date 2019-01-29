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
import org.eclipse.hono.util.TenantConstants;
import org.eclipse.hono.util.TenantResult;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

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
                .opType(DocWriteRequest.OpType.CREATE);
        System.out.println("create : "+tenantId);
        client.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(final IndexResponse indexResponse) {
                System.out.println("created "+tenantId + indexResponse.status().getStatus());
                resultHandler.handle(Future.succeededFuture(TenantResult.from(indexResponse.status().getStatus())));
            }

            @Override
            public void onFailure(final Exception e) {
                System.out.println("ES failure ! ADD (l73)"+e.getMessage());
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CONFLICT)));
            }
        });
    }

    @Override
    public void update(final String tenantId, final JsonObject tenantObj, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        final UpdateRequest request = new UpdateRequest("tenants", "doc", tenantId)
                .doc(tenantObj, XContentType.JSON);
        System.out.println("UPDATE : "+tenantId + "with"+tenantObj);
        client.updateAsync(request, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(final UpdateResponse updateResponse) {
                System.out.println("update ok");
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
            }

            @Override
            public void onFailure(final Exception e) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_CONFLICT)));
            }
        });
    }

    @Override
    public void remove(final String tenantId, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        final DeleteRequest request = new DeleteRequest("tenants", "doc", tenantId);
        System.out.println("REMOVE "+tenantId);

        client.deleteAsync(request, RequestOptions.DEFAULT, new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(final DeleteResponse deleteResponse) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(deleteResponse.status().getStatus())));
            }

            @Override
            public void onFailure(final Exception e) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            }
        });
    }

    @Override
    public void get(final String tenantId, final Span span, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        final GetRequest request = new GetRequest("tenants", "doc", tenantId);
        System.out.println("getting " + tenantId);
        client.getAsync(request, RequestOptions.DEFAULT, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                if (getResponse.isExists()) {
                    resultHandler.handle(Future.succeededFuture(
                            TenantResult.from(HttpURLConnection.HTTP_OK,
                                    JsonObject.mapFrom(getResponse.getSourceAsString()))));
                } else {
                    System.out.println("could not GET" + tenantId);
                    resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
                }
            }

            @Override
            public void onFailure(final Exception e) {
                e.printStackTrace();
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_INTERNAL_ERROR)));
            }
        });
    }

    @Override
    public void get(final X500Principal subjectDn, final Span span, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        System.out.println("getting by x509 " + subjectDn.getName());

        SearchRequest request = new SearchRequest("tenants");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.termQuery(TenantConstants.FIELD_PAYLOAD_SUBJECT_DN, subjectDn.toString()));
        request.source(searchSourceBuilder);

        client.searchAsync(request, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                if (searchResponse.getHits().totalHits != 0) {
                resultHandler.handle(Future.succeededFuture(
                        TenantResult.from(searchResponse.status().getStatus(),
                                JsonObject.mapFrom(searchResponse.getHits().getAt(0).getSourceAsString()))));
                 } else {
                    resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
                }
        }

            @Override
            public void onFailure(Exception e) {
                resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_INTERNAL_ERROR)));
            }
        });
    }

        @Override
    public void stop() throws Exception {
        client.close();
    }
}
