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

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.eclipse.hono.client.StatusCodeMapper;
import org.eclipse.hono.util.TenantObject;
import org.eclipse.hono.util.TenantResult;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class ESTenantServiceTest {

    private ESTenantServiceTest() {
    }

    public static void main(final String[] args) throws Exception {
        final ESTenantService svc = new ESTenantService();
        svc.setConfig(new ESTenantsConfigProperties());
        final CountDownLatch finish = new CountDownLatch(1);

        final Future<TenantResult<JsonObject>> result = Future.future();
        svc.add("test-tenant",
                JsonObject.mapFrom(TenantObject.from("test-tenant", true)),
                result.completer()
        );


        result.map(response -> {
            System.out.println(response.getStatus());
            finish.countDown();
            if (response.getStatus() == HttpURLConnection.HTTP_CREATED) {
                return null;
            } else {
                throw StatusCodeMapper.from(response);
            }
        });

        finish.await();
        svc.stop();

    }

}
