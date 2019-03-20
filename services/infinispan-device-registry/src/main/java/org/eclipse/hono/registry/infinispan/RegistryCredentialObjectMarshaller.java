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
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

/**
 * Defines how to serialize and de-serialize a RegistryCredentialObject for the infinispan hotrod client.
 * The protobuf schema is defined in resources/registry.proto.
 *
 */
public class RegistryCredentialObjectMarshaller implements MessageMarshaller<RegistryCredentialObject> {

    @Override
    public String getTypeName() {
        return "registry.RegistryCredentialObject";
    }

    @Override
    public Class<? extends RegistryCredentialObject> getJavaClass() {
        return RegistryCredentialObject.class;
    }

    @Override
    public void writeTo(final ProtoStreamWriter writer, final RegistryCredentialObject obj) throws IOException {
        writer.writeString("tenantId", obj.getTenantId());
        writer.writeString("deviceId", obj.getDeviceId());
        writer.writeString("originalJson", obj.getOriginalJson().encode());
    }

    @Override
    public RegistryCredentialObject readFrom(final ProtoStreamReader reader) throws IOException {
        final String tenantId = reader.readString("tenantId");
        final JsonObject json = new JsonObject(reader.readString("originalJson"));
        final CredentialsObject obj  = json.mapTo(CredentialsObject.class);
        return new RegistryCredentialObject(obj, tenantId, json);
    }
}
