package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.expedia.www.haystack.client.Span;

public class InMemoryDispatcher extends NoopDispatcher {
    private Semaphore limiter;
    private List<Span> reported;
    private List<Span> recieved;
    private List<Span> flushed;

    public InMemoryDispatcher() {
        this(Integer.MAX_VALUE);
    }

    public InMemoryDispatcher(int limit) {
        limiter = new Semaphore(limit);
        reported = new ArrayList<>();
        recieved = new ArrayList<>();
        flushed = new ArrayList<>();
    }

    @Override
    public void dispatch(Span span) {
        synchronized (this) {
            try {
                limiter.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            reported.add(span);
            recieved.add(span);
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            flushed.addAll(reported);
            reported.clear();
        }
    }

    public List<Span> getReportedSpans() {
        synchronized (this) {
            return Collections.unmodifiableList(reported);
        }
    }

    public List<Span> getFlushedSpans() {
        synchronized (this) {
            return Collections.unmodifiableList(flushed);
        }
    }

    public List<Span> getRecievedSpans() {
        synchronized (this) {
            return Collections.unmodifiableList(recieved);
        }
    }
}
