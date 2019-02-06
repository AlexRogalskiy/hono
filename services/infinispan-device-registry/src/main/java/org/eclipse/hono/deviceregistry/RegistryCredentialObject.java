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

import io.vertx.core.json.JsonObject;
import org.eclipse.hono.util.CredentialsObject;

public class RegistryCredentialObject {

    private final String tenantId;
    private final String deviceId;
    private final JsonObject originalJson;

    public RegistryCredentialObject(CredentialsObject honoCredential, String tenantId, JsonObject originalJson){
        this.tenantId = tenantId;
        this.deviceId = honoCredential.getDeviceId();
        this.originalJson = originalJson;
    }

    public JsonObject getOriginalJson() {
        return originalJson;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getDeviceId(){
        return deviceId;
    }
}
