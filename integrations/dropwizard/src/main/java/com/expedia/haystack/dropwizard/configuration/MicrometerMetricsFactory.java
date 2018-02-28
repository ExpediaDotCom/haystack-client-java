package com.expedia.haystack.dropwizard.configuration;

import com.expedia.www.haystack.client.metrics.GlobalMetricsRegistry;
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
@JsonTypeName("micrometer")
public class MicrometerMetricsFactory implements MetricsFactory {

    @Override
    public MetricsRegistry build() {
        return new GlobalMetricsRegistry();
    }
}
