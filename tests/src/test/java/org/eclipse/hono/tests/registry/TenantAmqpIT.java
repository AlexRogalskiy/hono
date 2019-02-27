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

package org.eclipse.hono.tests.registry;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import javax.security.auth.x500.X500Principal;

import org.eclipse.hono.client.HonoClient;
import org.eclipse.hono.client.ServiceInvocationException;
import org.eclipse.hono.client.TenantClient;
import org.eclipse.hono.tests.IntegrationTestSupport;
import org.eclipse.hono.util.TenantConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.proton.ProtonClientOptions;

/**
 * Tests verifying the behavior of the Device Registry component's Tenant AMQP endpoint.
 */
@RunWith(VertxUnitRunner.class)
public class TenantAmqpIT {

    private static final Vertx vertx = Vertx.vertx();

    private static HonoClient client;
    private static TenantClient tenantClient;
    private static IntegrationTestSupport helper;

    /**
     * Global timeout for all test cases.
     */
    @Rule
    public Timeout globalTimeout = new Timeout(5, TimeUnit.SECONDS);

    /**
     * Starts the device registry, connects a client and provides a tenant API client.
     * 
     * @param ctx The vert.x test context.
     */
    @BeforeClass
    public static void prepareDeviceRegistry(final TestContext ctx) {

        helper = new IntegrationTestSupport(vertx);
        helper.initRegistryClient(ctx);

        client = DeviceRegistryAmqpTestSupport.prepareDeviceRegistryClient(vertx,
                IntegrationTestSupport.HONO_USER, IntegrationTestSupport.HONO_PWD);

        client.connect(new ProtonClientOptions())
            .compose(c -> c.getOrCreateTenantClient())
            .setHandler(ctx.asyncAssertSuccess(r -> {
                tenantClient = r;
            }));
    }

    /**
     * Removes all temporary objects from the registry.
     *
     * @param ctx The vert.x test context.
     */
    @After
    public void cleanUp(final TestContext ctx) {
        helper.deleteObjects(ctx);
    }

    /**
     * Shuts down the device registry and closes the client.
     * 
     * @param ctx The vert.x test context.
     */
    @AfterClass
    public static void shutdown(final TestContext ctx) {

        DeviceRegistryAmqpTestSupport.shutdownDeviceRegistryClient(ctx, vertx, client);

    }

    /**
     * Verifies that a client can use the get operation to retrieve information for an existing
     * tenant.
     * 
     * @param ctx The vert.x test context.
     */
    @Test
    public void testGetTenant(final TestContext ctx) {

        // Prepare the identities to insert
        final String tenantId = helper.getRandomTenantId();

        final Async done = ctx.async();
        // Insert into the device Registry
        helper.registry.addTenant(new JsonObject()
                .put(TenantConstants.FIELD_PAYLOAD_TENANT_ID, tenantId)
                .put(TenantConstants.FIELD_ENABLED, true)
                .put(TenantConstants.FIELD_PAYLOAD_TRUSTED_CA, new JsonObject()
                        .put(TenantConstants.FIELD_PAYLOAD_SUBJECT_DN, "CN=ca,OU=Hono,O=Eclipse")
                        .put(TenantConstants.FIELD_PAYLOAD_PUBLIC_KEY, "NOTAPUBLICKEY")))

       .setHandler(r -> {
           // verify
           tenantClient
               .get(tenantId)
               .setHandler(ctx.asyncAssertSuccess(tenantObject -> {
                   ctx.assertEquals(tenantId, tenantObject.getTenantId());
                   done.complete();
           }));
       });
        done.await();
    }

    /**
     * Verifies that a client cannot retrieve information for a tenant that he is not authorized to
     * get information for.
     *
     * @param ctx The vert.x test context.
     */
    @Test
    public void testGetNotConfiguredTenantReturnsUnauthorized(final TestContext ctx) {

        // Prepare the identities to insert
        final String tenantId = helper.getRandomTenantId();

        // create payload
        final JsonObject payload = new JsonObject()
                .put(TenantConstants.FIELD_PAYLOAD_TENANT_ID, tenantId)
                .put(TenantConstants.FIELD_ENABLED, true)
                .put(TenantConstants.FIELD_ADAPTERS, new JsonArray()
                        .add(new JsonObject()
                                .put(TenantConstants.FIELD_ADAPTERS_TYPE, "hono-http")
                                .put(TenantConstants.FIELD_ENABLED, true)
                                .put(TenantConstants.FIELD_ADAPTERS_DEVICE_AUTHENTICATION_REQUIRED, true)));

        final Async done = ctx.async();
        // Insert into the device Registry
        helper.registry.addTenant(payload).setHandler(r -> {
            // verify
            tenantClient
                    .get(tenantId)
                    .setHandler(ctx.asyncAssertFailure(t -> {
                        ctx.assertEquals(
                                HttpURLConnection.HTTP_FORBIDDEN,
                                ((ServiceInvocationException) t).getErrorCode());
                        done.complete();
                    }));
        });
        done.await();
    }

    /**
     * Verifies that a request to retrieve information for a non existing tenant
     * fails with a {@link HttpURLConnection#HTTP_NOT_FOUND}.
     *
     * @param ctx The vert.x test context.
     */
    @Test
    public void testGetConfiguredButNotCreatedTenantReturnsNotFound(final TestContext ctx) {

        tenantClient
                .get("NON_EXISTING_TENANT")
                .setHandler(ctx.asyncAssertFailure(t -> {
                    ctx.assertEquals(
                            HttpURLConnection.HTTP_NOT_FOUND,
                            ((ServiceInvocationException) t).getErrorCode());
                }));
    }

    /**
     * Verifies that a client can use the getByCa operation to retrieve information for
     * a tenant by the subject DN of the trusted certificate authority.
     *
     * @param ctx The vert.x test context.
     */
    @Test
    public void testGetByCaSucceeds(final TestContext ctx) {

        // Prepare the identities to insert
        final String tenantId = helper.getRandomTenantId();

        // create payload
        final JsonObject payload = new JsonObject()
                .put(TenantConstants.FIELD_PAYLOAD_TENANT_ID, tenantId)
                .put(TenantConstants.FIELD_ENABLED, true)
                .put(TenantConstants.FIELD_PAYLOAD_TRUSTED_CA, new JsonObject()
                        .put(TenantConstants.FIELD_PAYLOAD_SUBJECT_DN, "CN=ca,OU=Hono,O=Eclipse")
                        .put(TenantConstants.FIELD_PAYLOAD_PUBLIC_KEY, "NOTAPUBLICKEY"));

        final X500Principal subjectDn = new X500Principal("CN=ca, OU=Hono, O=Eclipse");

        final Async done = ctx.async();
        // Insert into the device Registry
        helper.registry.addTenant(payload).setHandler(r -> {
                    // verify
                    tenantClient
                            .get(subjectDn)
                            .setHandler(ctx.asyncAssertSuccess(tenantObject -> {
                                ctx.assertEquals(tenantId, tenantObject.getTenantId());
                                final JsonObject trustedCa = tenantObject.getProperty(TenantConstants.FIELD_PAYLOAD_TRUSTED_CA);
                                ctx.assertNotNull(trustedCa);
                                final X500Principal trustedSubjectDn = new X500Principal(trustedCa.getString(TenantConstants.FIELD_PAYLOAD_SUBJECT_DN));
                                ctx.assertEquals(subjectDn, trustedSubjectDn);
                                done.complete();
                            }));
                    });
        done.await();
    }

    /**
     * Verifies that a request to retrieve information for a tenant by the
     * subject DN of the trusted certificate authority fails with a
     * <em>403 Forbidden</em> if the client is not authorized to retrieve
     * information for the tenant.
     *
     * @param ctx The vert.x test context.
     */
    @Test
    public void testGetByCaFailsIfNotAuthorized(final TestContext ctx) {

        // Prepare the identities to insert
        final String tenantId = helper.getRandomTenantId();

        // create payload
        final JsonObject payload = new JsonObject()
                .put(TenantConstants.FIELD_PAYLOAD_TENANT_ID, tenantId)
                .put(TenantConstants.FIELD_ENABLED, true)
                .put(TenantConstants.FIELD_PAYLOAD_TRUSTED_CA, new JsonObject()
                        .put(TenantConstants.FIELD_PAYLOAD_SUBJECT_DN, "CN=ca,OU=Hono,O=Eclipse")
                        .put(TenantConstants.FIELD_PAYLOAD_PUBLIC_KEY, "NOTAPUBLICKEY"));

        final X500Principal subjectDn = new X500Principal("CN=ca, OU=Hono, O=Eclipse");

        final Async done = ctx.async();
        // Insert into the device Registry
        helper.registry.addTenant(payload).setHandler(r -> {
            // verify
            tenantClient
                    .get(subjectDn)
                    .setHandler(ctx.asyncAssertFailure(t -> {
                        ctx.assertEquals(
                                HttpURLConnection.HTTP_FORBIDDEN,
                                ((ServiceInvocationException) t).getErrorCode());
                        done.complete();
                    }));
        });
        done.await();
    }
}
