package com.expedia.www.haystack.client.propagation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * URLCodex/Decoder for use with <code>TextMap</code> propagation.
 *
 */
public class TextMapURLCodex extends TextMapCodex {

    /**
     * Note: The World Wide Web Consortium Recommendation states that
     * UTF-8 should be used. Not doing so may introduce
     * incompatibilites.

     * @see java.net.URLCodex#encode(String, String)
     * @see java.net.URLDecoder#decode(String, String)
     */
    private final String ENCODING = "UTF-8";

    @Override
    public String encode(Object value) {
        try {
            return URLEncoder.encode(value.toString(), ENCODING);
        } catch (UnsupportedEncodingException e) {
            // hope for the best
            return value.toString();
        }
    }

    @Override
    public String decode(Object value) {
        try {
            return URLDecoder.decode(value.toString(), ENCODING);
        } catch (UnsupportedEncodingException e) {
            // hope for the best
            return value.toString();
        }
    }

}
