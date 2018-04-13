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
package com.expedia.haystack.dropwizard.configuration;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.ChainedDispatcher;
import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import io.dropwizard.setup.Environment;
import io.opentracing.noop.NoopTracerFactory;

public class TracerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TracerFactory.class);

    @Valid
    @NotNull
    private MetricsFactory metrics = new DropwizardMetricsFactory();

    @Valid
    private boolean enabled = true;

    @NotEmpty
    private String serviceName;

    @Valid
    @NotEmpty
    private List<DispatcherFactory> dispatchers = ImmutableList.of(new RemoteDispatcherFactory());

    public io.opentracing.Tracer build(Environment environment) {
        if (!enabled) {
            return NoopTracerFactory.create();
        }

        MetricsRegistry registry = metrics.build(environment);

        Dispatcher dispatcher;
        if (dispatchers.size() > 1) {
            ChainedDispatcher.Builder builder = new ChainedDispatcher.Builder();
            for (DispatcherFactory factory : dispatchers) {
                builder.withDispatcher(factory.build(environment, registry));
            }
            dispatcher = builder.build();
        } else {
            dispatcher = dispatchers.get(0).build(environment, registry);
        }

        final Tracer.Builder builder = new Tracer.Builder(registry, serviceName, dispatcher);
        return builder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("serviceName", serviceName)
            .add("dispatchers", dispatchers)
            .toString();
    }

    @JsonProperty
    public MetricsFactory getMetrics() {
        return metrics;
    }

    @JsonProperty
    public void setMetrics(MetricsFactory metrics) {
        this.metrics = metrics;
    }

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonProperty
    public List<DispatcherFactory> getDispatchers() {
        return dispatchers;
    }

    @JsonProperty
    public void setDispatchers(List<DispatcherFactory> dispatchers) {
        this.dispatchers = dispatchers;
    }
}
