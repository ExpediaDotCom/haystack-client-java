package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.expedia.www.haystack.client.Span;

public class InMemoryDispatcher extends NoopDispatcher {
    List<Span> spans;

    public InMemoryDispatcher() {
        this.spans = new ArrayList<>();
    }

    @Override
    public void dispatch(Span span) {
        synchronized (this) {
            spans.add(span);
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
