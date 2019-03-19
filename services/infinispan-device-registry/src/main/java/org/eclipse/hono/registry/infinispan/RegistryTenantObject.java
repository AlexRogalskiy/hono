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

import io.vertx.core.json.JsonObject;
import org.eclipse.hono.util.TenantObject;
import org.hibernate.search.annotations.Field;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A custom class to be used as value in the backend key-value storage.
 * This store tenants details.
 *
 *  See {@link CacheTenantService CacheTenantService} class.
 */
//@SerializeWith(RegistryTenantObject.Serializer.class)
public class RegistryTenantObject implements Serializable {

    @Field
    private final String tenantId;
    // Matching TenantConstants.FIELD_PAYLOAD_TRUSTED_CA;
    @Field
    private String trustedCa;

    private String tenantObject;


    /**
     * Create a a RegistryTenantObject with the Tenant details.
     * @param tenant the tenant object, in a {@link org.eclipse.hono.util.TenantObject Hono TenantObject util class}.
     */
    public RegistryTenantObject(final TenantObject tenant) {
        this.tenantId = tenant.getTenantId();

        if (tenant.getTrustedCaSubjectDn() != null ){
            this.trustedCa = tenant.getTrustedCaSubjectDn().getName();
        } else {
            this.trustedCa = null;
        }

        this.tenantObject = JsonObject.mapFrom(tenant).encode();
    }

    public TenantObject getTenantObject() {
        return new JsonObject(tenantObject).mapTo(TenantObject.class);
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTrustedCa() {
        return trustedCa;
    }
}
