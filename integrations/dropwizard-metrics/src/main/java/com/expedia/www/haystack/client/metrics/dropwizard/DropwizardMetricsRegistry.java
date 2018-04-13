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
package com.expedia.www.haystack.client.metrics.dropwizard;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

import com.codahale.metrics.MetricRegistry;
import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Gauge;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;

public class DropwizardMetricsRegistry implements MetricsRegistry {
    private final MetricRegistry registry;
    private final NameMapper nameMapper;

    public DropwizardMetricsRegistry(MetricRegistry registry, NameMapper nameMapper) {
        this.registry = registry;
        this.nameMapper = nameMapper;
    }

    @Override
    public <T> Gauge gauge(String name, Collection<Tag> tags, T obj, ToDoubleFunction<T> f) {
        com.codahale.metrics.Gauge<Double> gauge = () -> {
            if (obj != null) {
                return f.applyAsDouble(obj);
            }
            return Double.NaN;
        };
        registry.register(nameMapper.toName(name, tags), gauge);
        return new DropwizardGauge(gauge);
    }

    @Override
    public Counter counter(String name, Collection<Tag> tags) {
        return new DropwizardCounter(registry.meter(nameMapper.toName(name, tags)));
    }

    @Override
    public Timer timer(String name, Collection<Tag> tags) {
        return new DropwizardTimer(registry.timer(nameMapper.toName(name, tags)));
    }
}
