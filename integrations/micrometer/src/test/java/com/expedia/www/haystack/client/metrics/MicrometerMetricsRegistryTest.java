package com.expedia.www.haystack.client.metrics;

import static java.util.Collections.emptyList;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MicrometerMetricsRegistryTest {

    private final MetricsRegistry registry = new MicrometerMetricsRegistry(new SimpleMeterRegistry());

    @Test
    public void gaugeOnNullValue() {
        Gauge gauge = registry.gauge("gauge", emptyList(), null, obj -> 1.0);
        assertEquals(gauge.value(), Double.NaN, 0);
    }

    @Test
    public void metersOnNullTags() {
        Gauge gauge = registry.gauge("gauge", null, null, obj -> 1.0);
        assertEquals(gauge.value(), Double.NaN, 0);
        Counter counter = registry.counter("counter", null);
        assertEquals(counter.count(), 0, 0);
        Timer timer = registry.timer("timer", null);
        assertEquals(timer.count(), 0, 0);
    }
}
