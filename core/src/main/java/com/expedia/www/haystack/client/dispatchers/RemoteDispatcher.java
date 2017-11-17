package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final long shutdownTimeoutMillis;

    private final Future flushTask;
    private final CompletableFuture<Void> senderTask;

    private final AtomicBoolean running;

    public RemoteDispatcher(Client client, BlockingQueue<Span> queue, long flushInterval, long shutdownTimeout, ScheduledExecutorService executor) {
        this.client = client;
        this.acceptQueue = queue;
        this.executor = executor;
        this.shutdownTimeoutMillis = shutdownTimeout;

        this.running = new AtomicBoolean(true);

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

        this.senderTask = CompletableFuture.runAsync(() -> {
                while(running.get() || !(acceptQueue.isEmpty())) {
                    try {
                        Span span = acceptQueue.take();
                        try {
                            client.send(span);
                        } catch (ClientException e) {
                            LOGGER.error("Client reported a failure:", e);
                        }
                    } catch (InterruptedException e) {
                        // do nothing; will retry next interation
                    }
                }
            }, executor);

    }

    @Override
    public void dispatch(Span span) {
        if (running.get()) {
            final boolean accepted = acceptQueue.offer(span);
            if (!accepted) {
                LOGGER.warn("Send queue is rejecting new spans");
            }
        } else {
            LOGGER.warn("Dispatcher is shutting down and queue is now rejecting new spans");
        }
    }

    @Override
    public void close() throws IOException {
        running.set(false);

        try {
            senderTask.get(shutdownTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // failed to fully flush the queue so force canceling
            LOGGER.warn("Timeout attempting to fully empty the queue before shutting down");
            senderTask.cancel(true);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted attempting to shutting down");
            senderTask.cancel(true);
        } catch (CancellationException | ExecutionException e) {
            // do nothing, task was cancelled or finished with an exception
            LOGGER.warn("Sender task exited abnormally while shutting down", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                LOGGER.warn("Client failed to close", e);
            }

            flushTask.cancel(true);

            try {
                executor.shutdown();
            } catch (SecurityException e) {
                LOGGER.warn("Executor pool failed to close", e);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        client.flush();
    }

    public static final class Builder {
        private Client client;
        private BlockingQueue<Span> acceptQueue;
        private long flushInterval;
        private long shutdownTimeout;
        private ScheduledExecutorService executor;

        public Builder(Client client) {
            this.client = client;
            flushInterval = TimeUnit.MINUTES.toMillis(1);
            shutdownTimeout = TimeUnit.MINUTES.toMillis(1);
            executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        }

        public Builder withExecutor(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder withExecutorThreadCount(int threads) {
            executor = Executors.newScheduledThreadPool(threads);
            return this;
        }

        public Builder withBlockingQueue(BlockingQueue<Span> acceptQueue) {
            this.acceptQueue = acceptQueue;
            return this;
        }

        public Builder withBlockingQueueLimit(int limit) {
            this.acceptQueue = new ArrayBlockingQueue<>(limit);
            return this;
        }

        public Builder withFlushIntervalMillis(long flushInterval) {
            this.flushInterval = flushInterval;
            return this;
        }

        public Builder withShutdownTimeoutMillis(long shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }

        public RemoteDispatcher build() {
            return new RemoteDispatcher(client, acceptQueue, flushInterval, shutdownTimeout, executor);
        }
    }

}
