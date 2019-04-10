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

package com.expedia.www.haystack.remote.clients;

import com.expedia.open.tracing.agent.api.DispatchResult;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc;
import com.expedia.www.haystack.client.metrics.*;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

abstract public class BaseGrpcClient<R> implements Client<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseGrpcClient.class);

    protected final ManagedChannel channel;
    protected final SpanAgentGrpc.SpanAgentStub stub;
    protected final long shutdownTimeoutMS;
    protected final StreamObserver<DispatchResult> observer;

    protected final Timer sendTimer;
    protected final Counter sendExceptionCounter;
    protected final Timer closeTimer;
    protected final Counter closeTimeoutCounter;
    protected final Counter closeInterruptedCounter;
    protected final Counter closeExceptionCounter;
    protected final Counter flushCounter;

    public BaseGrpcClient(Metrics metrics,
                          ManagedChannel channel,
                          SpanAgentGrpc.SpanAgentStub stub, StreamObserver<DispatchResult> observer,
                          long shutdownTimeoutMS) {
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

    public void close() {
        try (Timer.Sample timer = closeTimer.start()) {
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

    public void flush() {
        flushCounter.increment();
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
            LOGGER.error("Dispatching span failed with error: " + t, t);
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

    public static abstract class Builder {
        protected StreamObserver<DispatchResult> observer;

        protected Metrics metrics;

        // Options to build a channel
        protected String host;
        protected int port;
        protected long keepAliveTimeMS = TimeUnit.SECONDS.toMillis(30);
        protected long keepAliveTimeoutMS = TimeUnit.SECONDS.toMillis(30);
        protected boolean keepAliveWithoutCalls = true;
        protected NegotiationType negotiationType = NegotiationType.PLAINTEXT;

        // either build a channel or provide one
        protected ManagedChannel channel;

        protected long shutdownTimeoutMS = TimeUnit.SECONDS.toMillis(30);

        private Builder(MetricsRegistry registry) {
            this(new Metrics(registry, Client.class.getName(), Arrays.asList(new Tag("type", "grpc"))));
        }

        private Builder(Metrics metrics) {
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

        protected ManagedChannel buildManagedChannel() {
            return NettyChannelBuilder.forAddress(host, port)
                    .keepAliveTime(keepAliveTimeMS, TimeUnit.MILLISECONDS)
                    .keepAliveTimeout(keepAliveTimeoutMS, TimeUnit.MILLISECONDS)
                    .keepAliveWithoutCalls(keepAliveWithoutCalls)
                    .negotiationType(negotiationType)
                    .build();
        }

        public abstract BaseGrpcClient build();
    }
}
