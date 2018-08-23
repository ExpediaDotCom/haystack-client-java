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
package com.expedia.www.haystack.client.propagation;

import io.opentracing.propagation.Format;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry to keep track of injectors and extractors for various
 * format types needed to propagate context values.
 */
public class PropagationRegistry {
    private final Map<Format<?>, Injector<?>> injectors;
    private final Map<Format<?>, Extractor<?>> extractors;


    public PropagationRegistry() {
        injectors = new HashMap<>();
        extractors = new HashMap<>();
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .toString();
    }

    public void clear() {
        injectors.clear();
        extractors.clear();
    }

    public <T> Injector<T> getInjector(Format<T> format) {
        return (Injector<T>) injectors.get(format);
    }

    public <T> void register(Format<T> format, Injector<T> injector) {
        injectors.put(format, injector);
    }

    public <T> Injector<T> deregisterInjector(Format<T> format) {
        return (Injector<T>) injectors.remove(format);
    }

    public <T> Extractor<T> getExtractor(Format<T> format) {
        return (Extractor<T>) extractors.get(format);
    }

    public <T> void register(Format<T> format, Extractor<T> extractor) {
        extractors.put(format, extractor);
    }

    public <T> Extractor<T> deregisterExtractor(Format<T> format) {
        return (Extractor<T>) extractors.remove(format);
    }

}
