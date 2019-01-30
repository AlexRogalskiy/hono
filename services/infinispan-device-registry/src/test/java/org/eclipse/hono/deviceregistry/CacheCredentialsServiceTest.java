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
import org.eclipse.hono.auth.SpringBasedHonoPasswordEncoder;
import org.eclipse.hono.service.credentials.AbstractCompleteCredentialsServiceTest;
import org.eclipse.hono.service.credentials.CompleteCredentialsService;
import org.eclipse.hono.service.tenant.AbstractCompleteTenantServiceTest;
import org.eclipse.hono.service.tenant.CompleteTenantService;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CacheCredentialsServiceTest extends AbstractCompleteCredentialsServiceTest {

    CacheCredentialService service;

    @Before
    public void setUp() {
        service = new CacheCredentialService(new SpringBasedHonoPasswordEncoder());
    }


    @Override
    public CompleteCredentialsService getCompleteCredentialsService() {
        return service;
    }

}
