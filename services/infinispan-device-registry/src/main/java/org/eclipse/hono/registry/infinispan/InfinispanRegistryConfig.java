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

import org.eclipse.hono.deviceregistry.ApplicationConfig;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Spring Boot configuration for the Device Registry application.
 *
 */
@Configuration
public class InfinispanRegistryConfig extends ApplicationConfig {

    /**
     * Create an Infinispan RemoteCacheManager.
     * The constructor will use the hotrod-client.properties file that must be in the classpath.
     *
     * @return an RemoteCacheManager bean.
     */
    @Bean
    public RemoteCacheManager getCacheManager() throws IOException {

        RemoteCacheManager remoteCacheManager = new RemoteCacheManager();
        SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(remoteCacheManager);

        FileDescriptorSource fds = new FileDescriptorSource();
        fds.addProtoFiles("resources/library.proto");
        serCtx.registerProtoFiles(fds);
        serCtx.registerMarshaller(new RegistryCredentialObjectMarshaller());
        serCtx.registerMarshaller(new RegistryTenantObjectMarshaller());
        serCtx.registerMarshaller(new CredentialKeyMarshaller());
        serCtx.registerMarshaller(new RegistrationKeyMarshaller());
        return remoteCacheManager;
    }
}
