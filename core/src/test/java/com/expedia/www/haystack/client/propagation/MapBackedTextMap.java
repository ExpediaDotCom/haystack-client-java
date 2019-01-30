package com.expedia.www.haystack.client.propagation;

import io.opentracing.propagation.TextMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class MapBackedTextMap implements TextMap {
    private final Map<String, String> map = new HashMap<>();

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String get(final String key) {
        return map.get(key);
    }
}
