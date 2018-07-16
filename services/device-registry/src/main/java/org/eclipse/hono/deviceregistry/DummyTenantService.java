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
import org.eclipse.hono.service.tenant.BaseTenantService;
import org.eclipse.hono.util.TenantObject;
import org.eclipse.hono.util.TenantResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;

/**
 *
 */
@Service
@ConditionalOnProperty(name = "hono.app.type", havingValue = "dummy")
public class DummyTenantService extends BaseTenantService<Object> {

    @Override
    public void setConfig(final Object configuration) {
    }

    @Override
    public void get(final String tenantId, final Handler<AsyncResult<TenantResult<JsonObject>>> resultHandler) {
        final TenantObject tenant = new TenantObject();
        tenant.setTenantId(tenantId);
        tenant.setEnabled(true);
        resultHandler.handle(Future.succeededFuture(TenantResult.from(HttpURLConnection.HTTP_OK,
                JsonObject.mapFrom(tenant),
                null)));
    }
}
