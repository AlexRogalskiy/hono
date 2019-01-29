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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eclipse.hono.client.StatusCodeMapper;
import org.eclipse.hono.service.tenant.CompleteTenantService;
import org.eclipse.hono.util.Constants;
import org.eclipse.hono.util.TenantObject;
import org.eclipse.hono.util.TenantResult;
import org.eclipse.hono.service.tenant.AbstractCompleteTenantServiceTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class ESTenantServiceTest extends AbstractCompleteTenantServiceTest {

    /**
     * Time out each test after five seconds.
     */
    @Rule
    public final Timeout timeout = Timeout.seconds(30);

    private Vertx vertx;
    private EventBus eventBus;
    private ESTenantsConfigProperties props;
    private ESTenantService svc;

    /**
     * Sets up fixture.
     */
    @Before
    public void setUp() {
        final Context ctx = mock(Context.class);
        eventBus = mock(EventBus.class);
        vertx = mock(Vertx.class);
        when(vertx.eventBus()).thenReturn(eventBus);

        props = new ESTenantsConfigProperties();
        svc = new ESTenantService();
        svc.setConfig(props);
        svc.init(vertx, ctx);

        try {
            cleanDatabase();
        } catch (Exception e) {
            System.out.println("could not wipe ES database. Reason " + e.getMessage());
        }
    }

    private void cleanDatabase() throws Exception{
        //curl -XDELETE localhost:9299/*
        URL url = new URL("http://localhost:9299/*");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.getInputStream();
        final CountDownLatch countDown = new CountDownLatch(1);
        addTenant(Constants.DEFAULT_TENANT).compose(ok -> addTenant("OTHER_TENANT"))
                .setHandler(event -> {countDown.countDown();});
        countDown.await();
    }


    @Override
    public CompleteTenantService getCompleteTenantService() {
        return svc;
    }

    @Test
    @Ignore
    @Override
    public void testUpdateTenantsSucceeds(final TestContext ctx) {}
}
