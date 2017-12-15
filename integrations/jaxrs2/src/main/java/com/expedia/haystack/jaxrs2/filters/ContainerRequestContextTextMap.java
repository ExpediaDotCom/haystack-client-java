package com.expedia.haystack.jaxrs2.filters;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.container.ContainerRequestContext;

import io.opentracing.propagation.TextMap;

public class ContainerRequestContextTextMap implements TextMap {
    private final ContainerRequestContext context;

    public ContainerRequestContextTextMap(ContainerRequestContext context) {
        this.context = context;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        final Iterator<Entry<String, List<String>>> headers = context.getHeaders().entrySet().iterator();

        return new Iterator<Entry<String, String>>() {
            @Override
            public Entry<String, String> next() {
                Entry<String, List<String>> next = headers.next();
                return new AbstractMap.SimpleImmutableEntry<String, String>(next.getKey(), next.getValue().get(0));
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("This is a read-only implementation");
            }

            @Override
            public boolean hasNext() {
                return headers.hasNext();
            }
        };
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("This is a read-only implementation");
    }
}
