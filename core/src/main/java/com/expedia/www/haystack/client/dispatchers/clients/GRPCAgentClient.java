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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.open.tracing.agent.api.DispatchResult;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc.SpanAgentStub;
import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.formats.Format;
import com.expedia.www.haystack.client.dispatchers.formats.ProtoBufFormat;
import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;
import com.expedia.www.haystack.client.metrics.Timer.Sample;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GRPCAgentClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(GRPCAgentClient.class);

    private final Format<com.expedia.open.tracing.Span> format;
    private final ManagedChannel channel;
    private final SpanAgentStub stub;
    private final long shutdownTimeoutMS;
    private final StreamObserver<DispatchResult> observer;

    private final Timer sendTimer;
    private final Counter sendExceptionCounter;
    private final Timer closeTimer;
    private final Counter closeTimeoutCounter;
    private final Counter closeInterruptedCounter;
    private final Counter closeExceptionCounter;
    private final Counter flushCounter;

    public GRPCAgentClient(Metrics metrics, Format<com.expedia.open.tracing.Span> format, ManagedChannel channel, SpanAgentStub stub, StreamObserver<DispatchResult> observer, long shutdownTimeoutMS) {
        this.format = format;
        this.channel = channel;
        this.stub = stub;
        this.shutdownTimeoutMS = shutdownTimeoutMS;
        this.observer = observer;

        this.sendTimer = Timer.builder("send").register(metrics);
        this.sendExceptionCounter = Counter.builder("send").tag(new Tag("state", "exception")).register(metrics);
        this.closeTimer = Timer.builder("close").register(metrics);
        this.closeTimeoutCounter = Counter.builder("close").tag(new Tag("state", "timeout")).register(metrics);
        this.closeInterruptedCounter = Counter.builder("close").tag(new Tag("state", "interrupted")).register(metrics);
        this.closeExceptionCounter = Counter.builder("close").tag(new Tag("state", "exception")).register(metrics);
        this.flushCounter = Counter.builder("flush").register(metrics);

    }

    public static class GRPCAgentClientStreamObserver implements StreamObserver<DispatchResult> {
        private Counter onCompletedCounter;
        private Counter onErrorCounter;
        private Counter ratelimitCounter;
        private Counter unknownCounter;
        private Counter badresultCounter;

        public GRPCAgentClientStreamObserver(Metrics metrics) {
            this.onCompletedCounter = Counter.builder("observer").tag(new Tag("state", "completed")).register(metrics);
            this.onErrorCounter = Counter.builder("observer").tag(new Tag("state", "error")).register(metrics);
            this.ratelimitCounter = Counter.builder("observer").tag(new Tag("state", "ratelimited")).register(metrics);
            this.unknownCounter = Counter.builder("observer").tag(new Tag("state", "unknown")).register(metrics);
            this.badresultCounter = Counter.builder("observer").tag(new Tag("state", "badresult")).register(metrics);
        }

        @Override
        public void onCompleted() {
            onCompletedCounter.increment();
            LOGGER.debug("Dispatching span completed");
        }

        @Override
        public void onError(Throwable t) {
            onErrorCounter.increment();
            LOGGER.error("Dispatching span failed with error: {}", t);
        }

        @Override
        public void onNext(DispatchResult value) {
            switch (value.getCode()) {
            case SUCCESS:
                // do nothing
                break;
            case RATE_LIMIT_ERROR:
                ratelimitCounter.increment();
                LOGGER.error("Rate limit error received from agent");
                break;
            case UNKNOWN_ERROR:
                unknownCounter.increment();
                LOGGER.error("Unknown error received from agent");
                break;
            default:
                badresultCounter.increment();
                LOGGER.error("Unknown result received from agent: {}", value.getCode());
            }
        }
    }

    @Override
    public boolean send(Span span) throws ClientException {
        try (Sample timer = sendTimer.start()) {
            stub.dispatch(format.format(span), observer);
        } catch (Exception e) {
            sendExceptionCounter.increment();
            throw new ClientException(e.getMessage(), e);
        }
        // always true
        return true;
    }

    @Override
    public void close() {
        try (Sample timer = closeTimer.start()) {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(shutdownTimeoutMS, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                    closeTimeoutCounter.increment();
                    LOGGER.warn("Channel failed to terminate, forcibly closing it.");
                    if (!channel.awaitTermination(shutdownTimeoutMS, TimeUnit.SECONDS)) {
                        closeTimeoutCounter.increment();
                        LOGGER.error("Channel failed to terminate.");
                    }
                }
            } catch (InterruptedException e) {
                closeInterruptedCounter.increment();
                LOGGER.error("Unable to close the channel.", e);
            }
        } catch (Exception e) {
            closeExceptionCounter.increment();
            LOGGER.error("Unexpected exception caught on client shutdown.", e);
            throw e;
        }
    }

    @Override
    public void flush() {
        flushCounter.increment();
    }

    public static final class Builder {
        private Format<com.expedia.open.tracing.Span> format;

        private StreamObserver<DispatchResult> observer;

        private Metrics metrics;

        // Options to build a channel
        private String host;
        private int port;
        private long keepAliveTimeMS = TimeUnit.SECONDS.toMillis(30);
        private long keepAliveTimeoutMS = TimeUnit.SECONDS.toMillis(30);
        private boolean keepAliveWithoutCalls = true;
        private NegotiationType negotiationType = NegotiationType.PLAINTEXT;

        // either build a channel or provide one
        private ManagedChannel channel;

        private long shutdownTimeoutMS = TimeUnit.SECONDS.toMillis(30);

        private Builder(MetricsRegistry registry) {
            this(new Metrics(registry, Client.class.getName(), Arrays.asList(new Tag("type", "grpc"))));
        }

        private Builder(Metrics metrics) {
            this.format = new ProtoBufFormat();
            this.observer = new GRPCAgentClientStreamObserver(metrics);
            this.metrics = metrics;

        }

        public Builder(MetricsRegistry metrics, ManagedChannel channel) {
            this(metrics);
            this.channel = channel;
        }

        public Builder(Metrics metrics, ManagedChannel channel) {
            this(metrics);
            this.channel = channel;
        }

        public Builder(MetricsRegistry metrics, String host, int port) {
            this(metrics);
            this.host = host;
            this.port = port;
        }

        public Builder(Metrics metrics, String host, int port) {
            this(metrics);
            this.host = host;
            this.port = port;
        }

        public Builder withFormat(Format<com.expedia.open.tracing.Span> format) {
            this.format = format;
            return this;
        }

        public Builder withObserver(StreamObserver<DispatchResult> observer) {
            this.observer = observer;
            return this;
        }

        public Builder withKeepAliveTimeMS(long keepAliveTimeMS) {
            this.keepAliveTimeMS = keepAliveTimeMS;
            return this;
        }

        public Builder withKeepAliveTimeoutMS(long keepAliveTimeoutMS) {
            this.keepAliveTimeoutMS = keepAliveTimeoutMS;
            return this;
        }

        public Builder withKeepAliveWithoutCalls(boolean keepAliveWithoutCalls) {
            this.keepAliveWithoutCalls = keepAliveWithoutCalls;
            return this;
        }

        public Builder withNegotiationType(NegotiationType negotiationType) {
            this.negotiationType = negotiationType;
            return this;
        }

        public Builder withShutdownTimeoutMS(long shutdownTimeoutMS) {
            this.shutdownTimeoutMS = shutdownTimeoutMS;
            return this;
        }

        public GRPCAgentClient build() {

            ManagedChannel managedChannel = channel;

            if (managedChannel == null) {
                managedChannel = NettyChannelBuilder.forAddress(host, port)
                    .keepAliveTime(keepAliveTimeMS, TimeUnit.MILLISECONDS)
                    .keepAliveTimeout(keepAliveTimeoutMS, TimeUnit.MILLISECONDS)
                    .keepAliveWithoutCalls(keepAliveWithoutCalls)
                    .negotiationType(negotiationType)
                    .build();
            }

            SpanAgentStub stub = SpanAgentGrpc.newStub(managedChannel);

            return new GRPCAgentClient(metrics, format, managedChannel, stub, observer, shutdownTimeoutMS);
        }
    }
}
