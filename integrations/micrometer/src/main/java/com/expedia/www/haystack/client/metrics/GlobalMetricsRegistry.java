package com.expedia.www.haystack.client.metrics;

import io.micrometer.core.instrument.Metrics;

public class GlobalMetricsRegistry extends MicrometerMetricsRegistry {

    public GlobalMetricsRegistry() {
        super(Metrics.globalRegistry);
    }
}
