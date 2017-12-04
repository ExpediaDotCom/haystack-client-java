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
