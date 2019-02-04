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

import org.eclipse.hono.util.CredentialsObject;

public class RegistryCredentialObject {

    private CredentialsObject honoCredential;
    private final String tenantId;
    private final String deviceId;


    public RegistryCredentialObject(CredentialsObject honoCredential, String tenantId){
        this.honoCredential = honoCredential;
        this.tenantId = tenantId;
        this.deviceId = honoCredential.getDeviceId();
    }

    public String getTenantId() {
        return tenantId;
    }

    public CredentialsObject getHonoCredential() {
        return honoCredential;
    }

    public void setHonoCredential(CredentialsObject honoCredential) {
        this.honoCredential = honoCredential;
    }

    public String getDeviceId(){
        return deviceId;
    }
}
