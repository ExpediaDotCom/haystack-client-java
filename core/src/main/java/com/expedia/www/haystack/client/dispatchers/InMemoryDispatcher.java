package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Gauge;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;
import com.expedia.www.haystack.client.metrics.Timer.Sample;

public class InMemoryDispatcher implements Dispatcher {
    private Semaphore limiter;
    private List<Span> reported;
    private List<Span> recieved;
    private List<Span> flushed;

    private final Counter closeCounter;
    private final Timer dispatchTimer;
    private final Timer flushTimer;

    public InMemoryDispatcher(Metrics metrics, int limit) {
        limiter = new Semaphore(limit);
        reported = new ArrayList<>();
        recieved = new ArrayList<>();
        flushed = new ArrayList<>();

        // held in the registry a reference; but we don't need a local reference
        Gauge.builder("reported", reported, Collection::size).register(metrics);
        Gauge.builder("recieved", recieved, Collection::size).register(metrics);
        Gauge.builder("flushed", flushed, Collection::size).register(metrics);

        this.flushTimer = Timer.builder("flush").register(metrics);
        this.dispatchTimer = Timer.builder("dispatch").register(metrics);
        this.closeCounter = Counter.builder("close").register(metrics);
    }

    @Override
    public void close() throws IOException {
        closeCounter.increment();
    }

    @Override
    public void dispatch(Span span) {
        synchronized (this) {
            try (Sample timer = dispatchTimer.start()) {
                try {
                    limiter.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                reported.add(span);
                recieved.add(span);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            try (Sample timer = flushTimer.start()) {
                flushed.addAll(reported);
                reported.clear();
            }
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

    public static class Builder {
        private Metrics metrics;
        private int limit;

        public Builder(Metrics metrics) {
            this.metrics = metrics;
            this.limit = Integer.MAX_VALUE;
        }

        public Builder(MetricsRegistry registry) {
            this(new Metrics(registry, "com.expedia.www.haystack.client.Dispatcher", Collections.singletonList(new Tag("type", "inmemory"))));
        }

        public Builder withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public InMemoryDispatcher build() {
            return new InMemoryDispatcher(metrics, limit);
        }
    }
}
