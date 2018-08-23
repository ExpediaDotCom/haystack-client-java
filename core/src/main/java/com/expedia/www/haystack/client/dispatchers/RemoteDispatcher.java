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

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.ClientException;
import com.expedia.www.haystack.client.metrics.*;
import com.expedia.www.haystack.client.metrics.Timer.Sample;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteDispatcher implements Dispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDispatcher.class);

    private final BlockingQueue<Span> acceptQueue;
    private final ScheduledExecutorService executor;
    private final Client client;
    private final long shutdownTimeoutMillis;

    private final Future flushTask;
    private final CompletableFuture<Void> senderTask;

    private final AtomicBoolean running;

    private final Timer sendTimer;
    private final Counter sendInterruptedCounter;
    private final Counter sendExceptionCounter;
    private final Timer dispatchTimer;
    private final Counter dispatchRejectedCounter;
    private final Timer closeTimer;
    private final Counter closeTimeoutCounter;
    private final Counter closeInterruptedCounter;
    private final Counter closeExceptionCounter;
    private final Timer flushTimer;

    public RemoteDispatcher(Metrics metrics, Client client, BlockingQueue<Span> queue, long flushInterval, long shutdownTimeout, ScheduledExecutorService executor) {
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
                } catch (ClientException e) {
                    // do nothing; will retry next inverval
                }
            }
        }, flushInterval, flushInterval, TimeUnit.MILLISECONDS);

        this.sendTimer = Timer.builder("send").register(metrics);
        this.sendInterruptedCounter = Counter.builder("send").tag(new Tag("state", "interrupted")).register(metrics);
        this.sendExceptionCounter = Counter.builder("send").tag(new Tag("state", "exception")).register(metrics);

        this.senderTask = CompletableFuture.runAsync(() -> {
            while (running.get() || !(acceptQueue.isEmpty())) {

                try (Sample timer = sendTimer.start()) {
                    Span span = acceptQueue.take();
                    try {
                        client.send(span);
                    } catch (ClientException e) {
                        sendExceptionCounter.increment();
                        LOGGER.error("Client reported a failure:", e);
                    }
                } catch (InterruptedException e) {
                    // do nothing; will retry next interation
                    sendInterruptedCounter.increment();
                }
            }
        }, executor);

        // held in the registry; but we don't need a local reference
        Gauge.builder("acceptQueue", acceptQueue, Collection::size)
                .register(metrics);
        Gauge.builder("running", running, (running) -> (running.get() ? 1 : 0))
                .register(metrics);

        this.dispatchTimer = Timer.builder("dispatch").register(metrics);
        this.dispatchRejectedCounter = Counter.builder("dispatch").tag(new Tag("state", "rejected")).register(metrics);

        this.closeTimer = Timer.builder("close").register(metrics);
        this.closeTimeoutCounter = Counter.builder("close").tag(new Tag("state", "timeout")).register(metrics);
        this.closeInterruptedCounter = Counter.builder("close").tag(new Tag("state", "interrupted")).register(metrics);
        this.closeExceptionCounter = Counter.builder("close").tag(new Tag("state", "exception")).register(metrics);

        this.flushTimer = Timer.builder("flush").register(metrics);
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .setExcludeFieldNames("acceptQueue", "executor", "flushTask", "senderTask")
                .toString();
    }

    @Override
    public void dispatch(Span span) {
        try (Sample timer = dispatchTimer.start()) {
            if (running.get()) {
                final boolean accepted = acceptQueue.offer(span);
                if (!accepted) {
                    dispatchRejectedCounter.increment();
                    LOGGER.warn("Send queue is rejecting new spans");
                }
            } else {
                dispatchRejectedCounter.increment();
                LOGGER.warn("Dispatcher is shutting down and queue is now rejecting new spans");
            }
        }
    }

    @Override
    public void close() {
        try (Sample timer = closeTimer.start()) {
            running.set(false);

            try {
                senderTask.get(shutdownTimeoutMillis, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // failed to fully flush the queue so force canceling
                closeTimeoutCounter.increment();
                LOGGER.warn("Timeout attempting to fully empty the queue before shutting down");
                senderTask.cancel(true);
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted attempting to shutting down");
                closeInterruptedCounter.increment();
                senderTask.cancel(true);
            } catch (CancellationException | ExecutionException e) {
                // do nothing, task was cancelled or finished with an exception
                closeExceptionCounter.increment();
                LOGGER.warn("Sender task exited abnormally while shutting down", e);
            } finally {
                client.close();
                flushTask.cancel(true);

                try {
                    executor.shutdown();
                } catch (SecurityException e) {
                    closeExceptionCounter.increment();
                    LOGGER.warn("Executor pool failed to close", e);
                }
            }
        }
    }

    @Override
    public void flush() {
        try (Sample timer = flushTimer.start()) {
            client.flush();
        }
    }

    public static final class Builder {
        private Metrics metrics;
        private Client client;
        private BlockingQueue<Span> acceptQueue;
        private long flushInterval;
        private long shutdownTimeout;
        private ScheduledExecutorService executor;

        public Builder(MetricsRegistry registry, Client client) {
            this(new Metrics(registry, Dispatcher.class.getName(), Arrays.asList(new Tag("type", "remote"))), client);
        }

        public Builder(Metrics metrics, Client client) {
            this.metrics = metrics;
            this.client = client;
            acceptQueue = new ArrayBlockingQueue<>(1000);
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
            return new RemoteDispatcher(metrics, client, acceptQueue, flushInterval, shutdownTimeout, executor);
        }
    }

}
