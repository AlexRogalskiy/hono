package org.eclipse.hono.deviceregistry;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eclipse.hono.service.registration.AbstractCompleteRegistrationServiceTest;
import org.eclipse.hono.service.registration.CompleteRegistrationService;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Before;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public class CacheRegistrationServiceTest extends AbstractCompleteRegistrationServiceTest {

    CacheRegistrationService service;

    @Before
    public void setUp() throws Exception {

        EmbeddedCacheManager manager = new DefaultCacheManager();
        service = new CacheRegistrationService(manager);
    }

    @Override
    public CompleteRegistrationService getCompleteRegistrationService() {
        return service;
    }


}