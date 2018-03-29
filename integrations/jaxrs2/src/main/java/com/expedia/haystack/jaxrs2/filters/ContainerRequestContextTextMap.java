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
