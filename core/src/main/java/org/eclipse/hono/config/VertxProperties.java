/**
 * Copyright (c) 2018 Red Hat Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc - initial creation
 */

package org.eclipse.hono.config;

import io.vertx.core.VertxOptions;

/**
 * Vertx properties.
 */
public class VertxProperties {

    private boolean preferNative = VertxOptions.DEFAULT_PREFER_NATIVE_TRANSPORT;

    /**
     * Prefer to use native networking or not.
     * 
     * @param preferNative {@code true} to prefer native networking, {@code false} otherwise.
     */
    public void setPreferNative(final boolean preferNative) {
        this.preferNative = preferNative;
    }

    /**
     * Configure the Vertx options according to our settings.
     * 
     * @param options The options to configure.
     */
    public void configureVertx(final VertxOptions options) {

        options.setPreferNativeTransport(this.preferNative);

    }

}
