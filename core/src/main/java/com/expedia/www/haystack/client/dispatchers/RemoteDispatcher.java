package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.ClientException;

public class RemoteDispatcher implements Dispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDispatcher.class);

    private final BlockingQueue<Span> acceptQueue;
    private final ScheduledExecutorService executor;
    private final Client client;

    private final Future flushTask;
    private final Future spanSenderTask;

    public RemoteDispatcher(Client client, int maxBufferCapacity, int flushInterval, int taskPoolSize) {
        this(client, new ArrayBlockingQueue<>(maxBufferCapacity), flushInterval, Executors.newScheduledThreadPool(taskPoolSize));
    }

    public RemoteDispatcher(Client client, BlockingQueue<Span> queue, int flushInterval, ScheduledExecutorService executor) {
        this.client = client;
        this.acceptQueue = queue;
        this.executor = executor;

        this.flushTask = executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        flush();
                    } catch (IOException e) {
                        // do nothing; will retry next inverval
                    }
                }
            }, flushInterval, flushInterval, TimeUnit.MILLISECONDS);

        this.spanSenderTask = executor.submit(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            Span span = acceptQueue.take();
                            try {
                                client.send(span);
                            } catch (ClientException e) {
                                LOGGER.error("Client reported a failure:", e);
                            }
                        } catch (InterruptedException e) {
                            // do nothing; will retry next interval
                        }
                    }
                }
            });
    }

    @Override
    public void dispatch(Span span) {
        acceptQueue.offer(span);
    }

    @Override
    public void close() throws IOException {
        spanSenderTask.cancel(true);
        client.close();
        flushTask.cancel(true);
        executor.shutdown();
    }

    @Override
    public void flush() throws IOException {
        client.flush();
    }

}
