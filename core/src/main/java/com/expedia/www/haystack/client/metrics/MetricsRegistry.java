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
