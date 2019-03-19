package org.eclipse.hono.registry.infinispan;

import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class RegistrationKeyMarshaller implements MessageMarshaller<RegistrationKey> {

    @Override
    public String getTypeName() {
        return "registry.RegistrationKey";
    }

    @Override
    public Class<? extends RegistrationKey> getJavaClass() {
        return RegistrationKey.class;
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, RegistrationKey obj) throws IOException {
        writer.writeString("tenantId", obj.tenantId);
        writer.writeString("deviceId", obj.deviceId);
    }

    @Override
    public RegistrationKey readFrom(ProtoStreamReader reader) throws IOException {

        final String tenantId = reader.readString("tenantId");
        final String deviceId = reader.readString("deviceId");
        return new RegistrationKey(tenantId, deviceId);
    }
}