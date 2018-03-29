/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.client.dispatchers.clients;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Gauge;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;
import com.expedia.www.haystack.client.metrics.Timer.Sample;

public class InMemoryClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryClient.class);

    private Semaphore limiter;
    private List<Span> total;
    private List<Span> received;
    private List<Span> flushed;

    private final Timer sendTimer;
    private final Counter sendExceptionCounter;
    private final Timer closeTimer;
    private final Timer flushTimer;

    public InMemoryClient(Metrics metrics, int limit) {
        limiter = new Semaphore(limit);
        total = new ArrayList<>();
        received = new ArrayList<>();
        flushed = new ArrayList<>();

        this.sendTimer = Timer.builder("send").register(metrics);
        this.sendExceptionCounter = Counter.builder("send").tag(new Tag("state", "exception")).register(metrics);
        this.closeTimer = Timer.builder("close").register(metrics);
        this.flushTimer = Timer.builder("flush").register(metrics);

        // held in the registry a reference; but we don't need a local reference
        Gauge.builder("total", total, Collection::size).register(metrics);
        Gauge.builder("received", received, Collection::size).register(metrics);
        Gauge.builder("flushed", flushed, Collection::size).register(metrics);
    }

    @Override
    public boolean send(Span span) {
        LOGGER.info("Span sent to client: " + span);
        try (Sample timer = sendTimer.start()) {
            limiter.acquire();
            total.add(span);
            received.add(span);
            return true;
        } catch (InterruptedException e) {
            sendExceptionCounter.increment();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try (Sample timer = closeTimer.start()) {
            LOGGER.info("Client closed");
            flush();
        }
    }

    @Override
    public void flush() {
        try (Sample timer = flushTimer.start()) {
            LOGGER.info("Client flushed");
            flushed.addAll(received);
            received.clear();
        }
    }

    public List<Span> getTotalSpans() {
        return Collections.unmodifiableList(total);
    }

    public List<Span> getFlushedSpans() {
        return Collections.unmodifiableList(flushed);
    }

    public List<Span> getReceivedSpans() {
        return Collections.unmodifiableList(received);
    }

    public static final class Builder {
        private final Metrics metrics;
        private int limit;

        public Builder(MetricsRegistry registry) {
            this(new Metrics(registry, Client.class.getName(), Arrays.asList(new Tag("type", "inmemory"))));
        }

        public Builder(Metrics metrics) {
            this.metrics = metrics;
            this.limit = Integer.MAX_VALUE;
        }

        public Builder withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public InMemoryClient build() {
            return new InMemoryClient(metrics, limit);
        }
    }
}
