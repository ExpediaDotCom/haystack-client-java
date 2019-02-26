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
import java.util.Comparator;

import com.codahale.metrics.MetricRegistry;
import com.expedia.www.haystack.client.metrics.Tag;

/**
 * Define how to map to convert name and tags into a dropwizard style name
 */
public interface NameMapper {

    /**
     * Convert tagged metrics, with natural sorting, into dot seperated
     * names, e.g. {@code com.expedia.www.haystack.client.Tracer.flush.state.exception}
     */
    NameMapper DEFAULT = (name, tags) -> {
        String descriptors = new String();
        if (tags != null && !tags.isEmpty()) {
            descriptors = tags.stream()
                .sorted(Comparator.comparing(Tag::key))
                .map(t -> MetricRegistry.name(t.key(), t.value()))
                .reduce("", MetricRegistry::name);
        }

        return MetricRegistry.name(name, descriptors).replaceAll("[ ]", "_");
    };

    String toName(String name, Collection<Tag> tags);
}
