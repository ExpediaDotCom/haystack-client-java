package com.expedia.www.haystack.client.metrics;

import java.util.ArrayList;
import java.util.Collection;

public interface Counter {
    static Builder builder(String name) {
        return new Builder(name);
    }

    default void increment() {
        increment(1);
    }

    void increment(double amount);

    default void decrement() {
        decrement(1);
    }

    void decrement(double amount);

    double count();

    class Builder {
        private final String name;
        private final Collection<Tag> tags = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder tags(Collection<Tag> tags) {
            this.tags.addAll(tags);
            return this;
        }

        public Builder tag(Tag tag) {
            tags.add(tag);
            return this;
        }

        public Counter register(MetricsRegistry registry) {
            return registry.counter(name, tags);
        }
    }
}
