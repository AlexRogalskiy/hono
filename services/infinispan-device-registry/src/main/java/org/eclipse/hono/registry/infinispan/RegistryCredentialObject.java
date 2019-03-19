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
import org.eclipse.hono.util.CredentialsObject;
import org.hibernate.search.annotations.Field;

import java.io.Serializable;

/**
 * A custom class to be used as value in the backend key-value storage.
 * This store credentials details.
 *
 *  See {@link CacheTenantService CacheTenantService} class.
 */
public class RegistryCredentialObject implements Serializable {

    @Field
    private final String tenantId;
    @Field
    private final String deviceId;
    private final String originalJson;

    /**
     * Create a a RegistryCredentialObject with the credentials details.
     *
     * @param honoCredential the credential object, in a {@link org.eclipse.hono.util.CredentialsObject Hono CredentialsObject util class}.
     * @param tenantId the tenant ID associated with the credential.
     * @param originalJson the raw JSON object contained in the original creation request.
     */
    public RegistryCredentialObject(final CredentialsObject honoCredential, final String tenantId, final JsonObject originalJson){
        this.tenantId = tenantId;
        this.deviceId = honoCredential.getDeviceId();
        this.originalJson = originalJson.encode();
    }

    public JsonObject getOriginalJson() {
        return new JsonObject(originalJson);
    }

    public String getDeviceId(){
        return deviceId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
