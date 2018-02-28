package com.expedia.www.haystack.client.metrics;

public class Tag {
    private String key;
    private String value;

    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }
}

