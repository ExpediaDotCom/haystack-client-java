package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;

import com.expedia.www.haystack.client.Span;

public class NoopDispatcher implements Dispatcher {

    @Override
    public void dispatch(Span span) {
        // do nothing
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

}
