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

import java.util.Iterator;
import java.util.Map.Entry;

import javax.ws.rs.client.ClientRequestContext;

import io.opentracing.propagation.TextMap;

public class ClientRequestContextTextMap implements TextMap {
    private final ClientRequestContext context;

    public ClientRequestContextTextMap(ClientRequestContext context) {
        this.context = context;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("This is a write-only implementation");
    }

    @Override
    public void put(String key, String value) {
        context.getHeaders().putSingle(key, value);
    }

}
