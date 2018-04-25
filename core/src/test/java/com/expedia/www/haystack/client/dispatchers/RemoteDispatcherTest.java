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
package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.clients.InMemoryClient;
import com.expedia.www.haystack.client.metrics.LoggingMetricsRegistry;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;

public class RemoteDispatcherTest {
    private MetricsRegistry metrics;
    private Dispatcher dispatcher;
    private Tracer tracer;
    private InMemoryClient client;
    private final long flushInterval = TimeUnit.MILLISECONDS.toMillis(250);
    private final int queueSize = 100;

    @Before
    public void setUp() {
        metrics = new LoggingMetricsRegistry();
        client = new InMemoryClient.Builder(metrics).build();

        dispatcher = new RemoteDispatcher.Builder(metrics, client)
            .withFlushIntervalMillis(flushInterval)
            .withBlockingQueueLimit(queueSize)
            .build();

        tracer = new Tracer.Builder(metrics, "remote-dispatcher", dispatcher).build();
    }

    @Test
    public void testFlushTimer() {
        Span span = tracer.buildSpan("happy-path").start();
        dispatcher.dispatch(span);

        Awaitility.await()
            .atMost(flushInterval * 2, TimeUnit.MILLISECONDS)
            .until(() -> client.getFlushedSpans().size() > 0);

        Assert.assertEquals(0, client.getReceivedSpans().size());
        Assert.assertEquals(1, client.getFlushedSpans().size());
        Assert.assertEquals(1, client.getTotalSpans().size());
    }

    @Test
    public void testCloseDrainsTheQueue() throws IOException {
        final int createdSpans = queueSize;
        for (int i = 0; i < createdSpans; i++) {
            Span span = tracer.buildSpan("close-span-" + i).start();
            dispatcher.dispatch(span);
        }
        dispatcher.close();

        Assert.assertEquals(0, client.getReceivedSpans().size());
        Assert.assertEquals(createdSpans, client.getTotalSpans().size());
        Assert.assertEquals(createdSpans, client.getFlushedSpans().size());
    }

    @Test
    public void testWhenClientBlocks() throws IOException {
        // client allows zero messages and blocks
        client = new InMemoryClient.Builder(metrics).withLimit(0).build();

        dispatcher = new RemoteDispatcher.Builder(metrics, client)
            .withFlushIntervalMillis(flushInterval)
            .withShutdownTimeoutMillis(flushInterval * 2)
            .withBlockingQueueLimit(queueSize)
            .build();

        tracer = new Tracer.Builder(metrics, "remote-dispatcher", dispatcher).build();

        final int createdSpans = queueSize + 20;
        for (int i = 0; i < createdSpans; i++) {
            Span span = tracer.buildSpan("blocked-span-" + i).start();
            dispatcher.dispatch(span);
        }

        dispatcher.close();

        Assert.assertEquals(0, client.getTotalSpans().size());
        Assert.assertEquals(0, client.getFlushedSpans().size());
        Assert.assertEquals(0, client.getReceivedSpans().size());
    }

    @Test
    public void testClosedDispatcherRejectsAdditionalSpans() throws IOException {
        Span span = tracer.buildSpan("happy-path").start();
        dispatcher.dispatch(span);
        dispatcher.close();

        span = tracer.buildSpan("rejected-span").start();
        dispatcher.dispatch(span);

        Assert.assertEquals(0, client.getReceivedSpans().size());
        Assert.assertEquals(1, client.getTotalSpans().size());
        Assert.assertEquals(1, client.getFlushedSpans().size());
    }

    @Test
    public void testBuilderDefaults() throws IOException {
        dispatcher = new RemoteDispatcher.Builder(metrics, client).build();
        tracer = new Tracer.Builder(metrics, "remote-dispatcher", dispatcher).build();

        Span span = tracer.buildSpan("happy-path").start();
        dispatcher.dispatch(span);
        dispatcher.close();

        Assert.assertEquals(0, client.getReceivedSpans().size());
        Assert.assertEquals(1, client.getTotalSpans().size());
        Assert.assertEquals(1, client.getFlushedSpans().size());
    }
}
