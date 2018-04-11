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
package com.expedia.www.haystack.client.metrics.micrometer;

import java.util.Collection;
import java.util.Collections;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Gauge;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;

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
