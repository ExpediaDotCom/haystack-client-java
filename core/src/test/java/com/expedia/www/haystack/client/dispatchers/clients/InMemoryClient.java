package com.expedia.www.haystack.client.dispatchers.clients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.expedia.www.haystack.client.Span;

public class InMemoryClient extends NoopClient {
    private List<Span> spans;

    public InMemoryClient() {
        spans = new ArrayList<>();
    }

    @Override
    public boolean send(Span span) {
        synchronized (this) {
            spans.add(span);
            return true;
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            spans.clear();
        }
    }

    public List<Span> getReportedSpans() {
        synchronized (this) {
            return Collections.unmodifiableList(spans);
        }
    }
}
