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
