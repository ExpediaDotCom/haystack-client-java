package com.expedia.www.haystack.client.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.function.ToDoubleFunction;

public interface MetricsRegistry {

    default <T> Gauge gauge(String name, T obj, ToDoubleFunction<T> f) {
        return gauge(name, Collections.emptyList(), obj, f);
    }

    <T> Gauge gauge(String name, Collection<Tag> tags, T obj, ToDoubleFunction<T> f);

    default Counter counter(String name) {
        return counter(name, Collections.emptyList());
    }

    Counter counter(String name, Collection<Tag> tags);

    default Timer timer(String name) {
        return timer(name, Collections.emptyList());
    }

    Timer timer(String name, Collection<Tag> tags);
}
