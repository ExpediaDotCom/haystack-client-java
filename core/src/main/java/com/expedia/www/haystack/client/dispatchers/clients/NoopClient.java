package com.expedia.www.haystack.client.dispatchers.clients;

import java.io.IOException;

import com.expedia.www.haystack.client.Span;

public class NoopClient implements Client {

    public NoopClient() {
    }

    @Override
    public void flush() throws IOException {
        // does nothing
    }

    @Override
    public void close() throws IOException {
        // does nothing
    }

    @Override
    public boolean send(Span span) {
        // does nothing
        return true;
    }

}
