/*
 * Copyright (c) 2010-2026 Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: Eclipse Dirigible contributors SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.components.odata.config;

import org.eclipse.dirigible.components.base.http.uri.HostedEngineUris;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Declares the OData URL prefixes to the hosting platform. Because the OData engine is packaged
 * outside the platform, the platform's security chain and request filters no longer name
 * {@code /odata}; this contribution supplies those patterns when the engine is on the classpath.
 */
@Component
public class ODataHostedEngineUris implements HostedEngineUris {

    /**
     * The Ant-style pattern that marks the OData surface as authenticated.
     *
     * @return the secured Ant patterns
     */
    @Override
    public Collection<String> securedAntPatterns() {
        return List.of("/odata/**");
    }

    /**
     * The servlet-style URL pattern the platform request and security filters must cover, matching the
     * Olingo servlet registered in {@link ODataConfig}.
     *
     * @return the servlet filter URL patterns
     */
    @Override
    public Collection<String> filterUrlPatterns() {
        return List.of("/odata/v2/*");
    }
}