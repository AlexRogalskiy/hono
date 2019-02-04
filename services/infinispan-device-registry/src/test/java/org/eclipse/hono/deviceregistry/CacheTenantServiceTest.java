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

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eclipse.hono.service.tenant.AbstractCompleteTenantServiceTest;
import org.eclipse.hono.service.tenant.CompleteTenantService;
import org.eclipse.hono.util.TenantObject;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CacheTenantServiceTest extends AbstractCompleteTenantServiceTest {

    CacheTenantService service;

    @Before
    public void setUp() {
        EmbeddedCacheManager manager = new DefaultCacheManager();
        Cache<String, TenantObject> tenantsCache = manager.createCache("tenants", new ConfigurationBuilder().build());
        service = new CacheTenantService(tenantsCache);
    }

    @Override
    public CompleteTenantService getCompleteTenantService() {
        return service;
    }
}