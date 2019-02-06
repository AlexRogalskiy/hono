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

import org.eclipse.hono.util.TenantObject;

public class RegistryTenantObject {

    //TODO add infinispan anotations
    private final String tenantId;
    // Matching TenantConstants.FIELD_PAYLOAD_TRUSTED_CA;
    private String trustedCa;

    private TenantObject tenantObject;

    public RegistryTenantObject(TenantObject tenant) {
        this.tenantId = tenant.getTenantId();

        if (tenant.getTrustedCaSubjectDn() != null ){
            this.trustedCa = tenant.getTrustedCaSubjectDn().getName();
        } else this.trustedCa = null;

        this.tenantObject = tenant;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTrustedCa() {
        return trustedCa;
    }

    public void setTrustedCa(String trustedCa) {
        this.trustedCa = trustedCa;
    }

    public TenantObject getTenantObject() {
        return tenantObject;
    }

    public void setTenantObject(TenantObject tenantObject) {
        this.tenantObject = tenantObject;
    }
}
