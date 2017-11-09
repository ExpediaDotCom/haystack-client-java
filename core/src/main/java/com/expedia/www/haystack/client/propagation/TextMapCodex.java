package com.expedia.www.haystack.client.propagation;

/**
 *  Default encoder/decoder for use with <code>TextMap</code> propagation.
 */
public class TextMapCodex implements Codex<String,Object> {

    @Override
    public String encode(Object value) {
        return value.toString();
    }

    @Override
    public String decode(Object value) {
        return value.toString();
    }
}
