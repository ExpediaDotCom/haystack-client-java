package com.expedia.haystack.dropwizard.configuration;

import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A factory for configuring and building {@link NoopMetricsRegistry} instances.
 *
 * All configaruation is ignored by the registry.
 *
 * See {@link MetricsFactory} for more options, if any.
 */
@JsonTypeName("noop")
public class NoopMetricsFactory implements MetricsFactory {

    @Override
    public MetricsRegistry build() {
        return new NoopMetricsRegistry();
    }
}
