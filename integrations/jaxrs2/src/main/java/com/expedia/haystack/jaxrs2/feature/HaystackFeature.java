/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.haystack.jaxrs2.feature;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.haystack.jaxrs2.filters.ClientFilter;
import com.expedia.haystack.jaxrs2.filters.ServerFilter;

import io.opentracing.Tracer;

@Provider
public class HaystackFeature implements Feature {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaystackFeature.class);

    private final Tracer tracer;

    public HaystackFeature(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean configure(FeatureContext context) {
        if (context.getConfiguration().isEnabled(this.getClass())) {
            return false;
        }

        switch (context.getConfiguration().getRuntimeType()) {
        case SERVER:
            context.register(new ServerFilter(tracer));
            break;
        case CLIENT:
            context.register(new ClientFilter(tracer));
            break;
        default:
            LOGGER.error("Unknown runtime ({}), not registering Haystack feature", context.getConfiguration().getRuntimeType());
            return false;
        }
        return true;
    }
}
