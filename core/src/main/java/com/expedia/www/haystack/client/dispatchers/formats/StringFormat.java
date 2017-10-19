package com.expedia.www.haystack.client.dispatchers.formats;

import com.expedia.www.haystack.client.Span;

public class StringFormat implements Format<String> {

    @Override
    public String format(Span span) {
        return span.toString();
    }

}
