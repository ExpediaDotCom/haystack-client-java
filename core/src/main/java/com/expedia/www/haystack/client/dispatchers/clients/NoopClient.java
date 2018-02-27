package com.expedia.www.haystack.client.dispatchers.clients;

import com.expedia.www.haystack.client.Span;

public class NoopClient implements Client {

    public NoopClient() {
    }

    @Override
    public void flush() {
        // does nothing
    }

    @Override
    public void close() {
        // does nothing
    }

    @Override
    public boolean send(Span span) {
        // does nothing
        return true;
    }

}
