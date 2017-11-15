package com.expedia.www.haystack.client.propagation;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.propagation.Format;

/**
 * Registry to keep track of injectors and extractors for various
 * format types needed to propagate context values.
 *
 */
public class PropagationRegistry {
    private final Map<Format<?>, Injector<?>> injectors;
    private final Map<Format<?>, Extractor<?>> extractors;


    public PropagationRegistry() {
        injectors = new HashMap<>();
        extractors = new HashMap<>();
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
