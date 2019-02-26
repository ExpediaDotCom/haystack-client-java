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
