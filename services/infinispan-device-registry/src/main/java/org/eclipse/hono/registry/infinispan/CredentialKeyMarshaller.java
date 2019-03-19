package org.eclipse.hono.registry.infinispan;

import io.vertx.core.json.JsonObject;
import org.eclipse.hono.util.TenantObject;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class CredentialKeyMarshaller implements MessageMarshaller<CredentialsKey> {

    @Override
    public String getTypeName() {
        return "registry.CredentialsKey";
    }

    @Override
    public Class<? extends CredentialsKey> getJavaClass() {
        return CredentialsKey.class;
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, CredentialsKey obj) throws IOException {
        writer.writeString("tenantId", obj.tenantId);
        writer.writeString("authId", obj.authId);
        writer.writeString("type", obj.type);
    }

    @Override
    public CredentialsKey readFrom(ProtoStreamReader reader) throws IOException {

        final String tenantId = reader.readString("tenantId");
        final String authId = reader.readString("authId");
        final String type = reader.readString("type");
        return new CredentialsKey(tenantId, authId, type);
    }
}