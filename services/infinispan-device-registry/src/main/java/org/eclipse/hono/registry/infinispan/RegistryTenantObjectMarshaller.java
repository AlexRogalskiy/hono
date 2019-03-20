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
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

/**
 * Defines how to serialize and de-serialize a RegistryTenantObject for the infinispan hotrod client.
 * The protobuf schema is defined in resources/registry.proto.
 *
 */
public class RegistryTenantObjectMarshaller implements MessageMarshaller<RegistryTenantObject> {

    @Override
    public String getTypeName() {
        return "registry.RegistryTenantObject";
    }

    @Override
    public Class<? extends RegistryTenantObject> getJavaClass() {
        return RegistryTenantObject.class;
    }

    @Override
    public void writeTo(final ProtoStreamWriter writer, final RegistryTenantObject obj) throws IOException {
        writer.writeString("tenantId", obj.getTenantId());
        writer.writeString("trustedCa", obj.getTrustedCa());
        writer.writeString("tenantObject", JsonObject.mapFrom(obj.getTenantObject()).encode());
    }

    @Override
    public RegistryTenantObject readFrom(final ProtoStreamReader reader) throws IOException {

        final String tenantObject = reader.readString("tenantObject");
        final TenantObject obj  = new JsonObject(tenantObject).mapTo(TenantObject.class);
        return new RegistryTenantObject(obj);
    }
}
