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
import java.util.Collections;
import java.util.function.ToDoubleFunction;

public class Metrics implements MetricsRegistry {
    private final MetricsRegistry registry;
    private final Collection<Tag> commonTags;
    private final String baseName;

    public Metrics(MetricsRegistry registry, String baseName, Collection<Tag> commonTags) {
        this.registry = registry;
        this.baseName = baseName;
        this.commonTags = Collections.unmodifiableCollection(commonTags);
    }

    public Metrics(MetricsRegistry registry) {
        this(registry, "", Collections.emptyList());
    }

    protected Collection<Tag> combineTags(Collection<Tag> tags) {
        Collection<Tag> combinedTags = new ArrayList<>(commonTags);
        combinedTags.addAll(tags);
        return combinedTags;
    }

    protected String formatName(String name) {
        return String.format("%s.%s", baseName, name);
    }

    @Override
    public <T> Gauge gauge(String name, T obj, ToDoubleFunction<T> f) {
        return gauge(name, commonTags, obj, f);
    }

    @Override
    public <T> Gauge gauge(String name, Collection<Tag> tags, T obj, ToDoubleFunction<T> f) {
        return registry.gauge(formatName(name), combineTags(tags), obj, f);
    }

    @Override
    public Counter counter(String name) {
        return counter(name, commonTags);
    }

    @Override
    public Counter counter(String name, Collection<Tag> tags) {
        return registry.counter(formatName(name), combineTags(tags));
    }

    @Override
    public Timer timer(String name) {
        return timer(name, commonTags);
    }

    @Override
    public Timer timer(String name, Collection<Tag> tags) {
        return registry.timer(formatName(name), combineTags(tags));
    }
}
