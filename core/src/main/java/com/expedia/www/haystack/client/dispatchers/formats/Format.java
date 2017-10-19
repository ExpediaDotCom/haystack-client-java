package com.expedia.www.haystack.client.dispatchers.formats;

import com.expedia.www.haystack.client.Span;

public interface Format<R> {
    R format(Span span);
}
