package com.expedia.www.haystack.client.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.ToDoubleFunction;

public interface Gauge {
    static <T> Builder<T> builder(String name, T obj, ToDoubleFunction<T> f) {
        return new Builder<>(name, obj, f);
    }

    double value();

    class Builder<T> {
        private final String name;
        private final ToDoubleFunction<T> f;
        private final Collection<Tag> tags = new ArrayList<>();

        private final T obj;

        private Builder(String name, T obj, ToDoubleFunction<T> f) {
            this.name = name;
            this.obj = obj;
            this.f = f;
        }

        public Builder<T> tags(Collection<Tag> tags) {
            this.tags.addAll(tags);
            return this;
        }

        public Builder<T> tag(Tag tag) {
            tags.add(tag);
            return this;
        }

        public Gauge register(MetricsRegistry registry) {
            return registry.gauge(name, tags, obj, f);
        }
    }
}
