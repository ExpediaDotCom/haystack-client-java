package com.expedia.www.haystack.client.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerMetricsRegistry implements MetricsRegistry {
    private final MeterRegistry registry;

    public MicrometerMetricsRegistry(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T> Gauge gauge(String name, Collection<Tag> tags, T obj, ToDoubleFunction<T> f) {
        return new MicrometerGauge<T>(io.micrometer.core.instrument.Gauge.builder(name, obj, f).tags(toTags(tags)).register(registry));
    }

    @Override
    public Counter counter(String name, Collection<Tag> tags) {
        return new MicrometerCounter(registry.counter(name, toTags(tags)));
    }

    @Override
    public Timer timer(String name, Collection<Tag> tags) {
        return new MicrometerTimer(registry, registry.timer(name, toTags(tags)));
    }

    protected Iterable<io.micrometer.core.instrument.Tag> toTags(Collection<Tag> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        return tags.stream().map((t) -> io.micrometer.core.instrument.Tag.of(t.key(), t.value())).collect(Collectors.toList());
    }
}
