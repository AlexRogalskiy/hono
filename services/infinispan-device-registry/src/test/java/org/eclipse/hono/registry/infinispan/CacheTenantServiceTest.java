/*******************************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.hono.registry.infinispan;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eclipse.hono.service.tenant.AbstractCompleteTenantServiceTest;
import org.eclipse.hono.service.tenant.CompleteTenantService;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Tests verifying behavior of {@link CacheTenantService}.
 *
 */
@RunWith(VertxUnitRunner.class)
public class CacheTenantServiceTest extends AbstractCompleteTenantServiceTest {

    private static RemoteCacheManager manager;
    private static CacheTenantService service;

    /**
     * Global timeout for all test cases.
     */
    @Rule
    public Timeout globalTimeout = new Timeout(5, TimeUnit.SECONDS);

    /**
     * Spin up the service using Infinispan EmbeddedCache.
     * @throws IOException if the Protobuf spec file cannot be found.
     */
    @Before
    public void setUp() throws IOException {
        manager = new RemoteCacheManager();
        final SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(manager);
        final FileDescriptorSource fds = new FileDescriptorSource();

        fds.addProtoFiles("registry.proto");
        serCtx.registerProtoFiles(fds);
        serCtx.registerMarshaller(new RegistryCredentialObjectMarshaller());
        serCtx.registerMarshaller(new RegistryTenantObjectMarshaller());
        serCtx.registerMarshaller(new CredentialKeyMarshaller());
        serCtx.registerMarshaller(new RegistrationKeyMarshaller());
        service = new CacheTenantService(manager);
    }

    /**
     *
     *
     * @return
     */
    @After
    public void cleanUp(){
        manager.getCache("tenants").clear();
    }

    @Override
    public CompleteTenantService getCompleteTenantService() {
        return service;
    }
}
