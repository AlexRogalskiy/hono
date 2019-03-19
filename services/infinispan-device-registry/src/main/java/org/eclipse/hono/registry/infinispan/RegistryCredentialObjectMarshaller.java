package org.eclipse.hono.registry.infinispan;

import io.vertx.core.json.JsonObject;
import org.eclipse.hono.util.CredentialsObject;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

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
    public void writeTo(ProtoStreamWriter writer, RegistryCredentialObject obj) throws IOException {
        writer.writeString("tenantId", obj.getTenantId());
        writer.writeString("deviceId", obj.getDeviceId());
        writer.writeString("originalJson", obj.getOriginalJson().encode());
    }

    @Override
    public RegistryCredentialObject readFrom(ProtoStreamReader reader) throws IOException {
        String tenantId = reader.readString("tenantId");
        JsonObject json = new JsonObject(reader.readString("originalJson"));
        CredentialsObject obj  = json.mapTo(CredentialsObject.class);
        return new RegistryCredentialObject(obj, tenantId, json);
    }
}