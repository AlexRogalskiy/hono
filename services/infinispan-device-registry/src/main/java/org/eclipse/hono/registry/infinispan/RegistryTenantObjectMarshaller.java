package org.eclipse.hono.registry.infinispan;

import io.vertx.core.json.JsonObject;
import org.eclipse.hono.util.TenantObject;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

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
    public void writeTo(ProtoStreamWriter writer, RegistryTenantObject obj) throws IOException {
        writer.writeString("tenantId", obj.getTenantId());
        writer.writeString("trustedCa", obj.getTrustedCa());
        writer.writeString("tenantObject", JsonObject.mapFrom(obj.getTenantObject()).encode());
    }

    @Override
    public RegistryTenantObject readFrom(ProtoStreamReader reader) throws IOException {

        String tenantObject = reader.readString("tenantObject");
        TenantObject obj  = new JsonObject(tenantObject).mapTo(TenantObject.class);
        return new RegistryTenantObject(obj);
    }
}