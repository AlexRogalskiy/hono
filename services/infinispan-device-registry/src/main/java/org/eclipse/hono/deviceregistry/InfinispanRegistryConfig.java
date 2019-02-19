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

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Spring Boot configuration for the Device Registry application.
 *
 */
@Configuration
public class InfinispanRegistryConfig extends ApplicationConfig {

    @Value("${infinispan.config.file}")
    private String infinispanConfigFile;

    private EmbeddedCacheManager cacheManager;

    @Bean
    public EmbeddedCacheManager getCacheManager() {
        return cacheManager;
    }

    @Annotation ???
    public void setCacheManager(){
        try{
            this.cacheManager = new DefaultCacheManager(infinispanConfigFile);
        } catch (IOException e){
            this.cacheManager = new DefaultCacheManager();
        }
    }
}