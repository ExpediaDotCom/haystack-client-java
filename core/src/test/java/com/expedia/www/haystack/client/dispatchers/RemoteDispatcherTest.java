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

public class RemoteDispatcherTest {
    private Dispatcher dispatcher;
    private Tracer tracer;
    private InMemoryClient client;
    private final long flushInterval = TimeUnit.MILLISECONDS.toMillis(250);
    private final int queueSize = 100;

    @Before
    public void setUp() {
        client = new InMemoryClient();

        dispatcher = new RemoteDispatcher.Builder(client)
            .withFlushIntervalMillis(flushInterval)
            .withBlockingQueueLimit(queueSize)
            .build();

        tracer = new Tracer.Builder("remote-dispatcher", dispatcher).build();
    }

    @Test
    public void testFlushTimer() {
        Span span = tracer.buildSpan("happy-path").startManual();
        dispatcher.dispatch(span);

        Awaitility.await()
            .atMost(flushInterval * 2, TimeUnit.MILLISECONDS)
            .until(() -> client.getFlushedSpans().size() > 0);

        Assert.assertEquals(0, client.getRecievedSpans().size());
        Assert.assertEquals(1, client.getFlushedSpans().size());
        Assert.assertEquals(1, client.getTotalSpans().size());
    }

    @Test
    public void testCloseDrainsTheQueue() throws IOException {
        final int createdSpans = queueSize;
        for (int i = 0; i < createdSpans; i++) {
            Span span = tracer.buildSpan("close-span-" + i).startManual();
            dispatcher.dispatch(span);
        }
        dispatcher.close();

        Assert.assertEquals(0, client.getRecievedSpans().size());
        Assert.assertEquals(createdSpans, client.getTotalSpans().size());
        Assert.assertEquals(createdSpans, client.getFlushedSpans().size());
    }

    @Test
    public void testWhenClientBlocks() throws IOException {
        // client allows zero messages and blocks
        client = new InMemoryClient(0);

        dispatcher = new RemoteDispatcher.Builder(client)
            .withFlushIntervalMillis(flushInterval)
            .withShutdownTimeoutMillis(flushInterval * 2)
            .withBlockingQueueLimit(queueSize)
            .build();

        tracer = new Tracer.Builder("remote-dispatcher", dispatcher).build();

        final int createdSpans = queueSize + 20;
        for (int i = 0; i < createdSpans; i++) {
            Span span = tracer.buildSpan("blocked-span-" + i).startManual();
            dispatcher.dispatch(span);
        }

        dispatcher.close();

        Assert.assertEquals(0, client.getTotalSpans().size());
        Assert.assertEquals(0, client.getFlushedSpans().size());
        Assert.assertEquals(0, client.getRecievedSpans().size());
    }

    @Test
    public void testClosedDispatcherRejectsAdditionalSpans() throws IOException {
        Span span = tracer.buildSpan("happy-path").startManual();
        dispatcher.dispatch(span);
        dispatcher.close();

        span = tracer.buildSpan("rejected-span").startManual();
        dispatcher.dispatch(span);

        Assert.assertEquals(0, client.getRecievedSpans().size());
        Assert.assertEquals(1, client.getTotalSpans().size());
        Assert.assertEquals(1, client.getFlushedSpans().size());
    }

    @Test
    public void testBuilderDefaults() throws IOException {
        dispatcher = new RemoteDispatcher.Builder(client).build();
        tracer = new Tracer.Builder("remote-dispatcher", dispatcher).build();

        Span span = tracer.buildSpan("happy-path").startManual();
        dispatcher.dispatch(span);
        dispatcher.close();

        Assert.assertEquals(0, client.getRecievedSpans().size());
        Assert.assertEquals(1, client.getTotalSpans().size());
        Assert.assertEquals(1, client.getFlushedSpans().size());
    }
}
