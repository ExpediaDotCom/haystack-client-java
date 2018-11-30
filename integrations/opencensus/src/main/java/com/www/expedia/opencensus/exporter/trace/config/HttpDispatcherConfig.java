/*
 *  Copyright 2018 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.www.expedia.opencensus.exporter.trace.config;

import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Map;

public class HttpDispatcherConfig extends DispatcherConfig {
    private final String host;
    private final Map<String, String> httpHeaders;

    public HttpDispatcherConfig(final String host) {
        this(host, Collections.emptyMap(), 5000);
    }
    public HttpDispatcherConfig(final String host,
                                final Map<String, String> httpHeaders,
                                final long shutdownTimeoutInMillis) {
        super(shutdownTimeoutInMillis);
        Validate.notEmpty(host, "haystack http host can't be empty");

        this.host = host;
        this.httpHeaders = httpHeaders == null ? Collections.emptyMap() : httpHeaders;
    }

    public String getHost() {
        return host;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    @Override
    public DispatchType getType() {
        return DispatchType.HTTP;
    }
}
