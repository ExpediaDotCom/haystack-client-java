package com.expedia.www.haystack.client.dispatchers.clients;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.open.tracing.agent.api.DispatchResult;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc.SpanAgentStub;
import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.formats.Format;
import com.expedia.www.haystack.client.dispatchers.formats.ProtoBufFormat;

import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GRPCAgentClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(GRPCAgentClient.class);

    private final Format<com.expedia.open.tracing.Span> format;
    private final ManagedChannel channel;
    private final SpanAgentStub stub;
    private final long shutdownTimeoutMS;
    private final StreamObserver<DispatchResult> observer;

    public GRPCAgentClient(Format<com.expedia.open.tracing.Span> format, ManagedChannel channel, SpanAgentStub stub, StreamObserver<DispatchResult> observer, long shutdownTimeoutMS) {
        this.format = format;
        this.channel = channel;
        this.stub = stub;
        this.shutdownTimeoutMS = shutdownTimeoutMS;
        this.observer = observer;
    }

    public static class GRPCAgentClientStreamObserver implements StreamObserver<DispatchResult> {
        @Override
        public void onCompleted() {
            LOGGER.info("Dispatching span completed");
        }

        @Override
        public void onError(Throwable t) {
            LOGGER.error("Dispatching span failed with error: {}", t);
        }

        @Override
        public void onNext(DispatchResult value) {
            switch (value.getCode()) {
            case SUCCESS:
                // do nothing
                break;
            case RATE_LIMIT_ERROR:
                LOGGER.error("Rate limit error recieved from agent");
                break;
            case UNKNOWN_ERROR:
                LOGGER.error("Unknown error recieved from agent");
                break;
            default:
                LOGGER.error("Unknown result recieved from agent: {}", value.getCode());
            }
        }
    }

    @Override
    public boolean send(Span span) throws ClientException {
        stub.dispatch(format.format(span), observer);
        // always true
        return true;
    }

    @Override
    public void close() throws IOException {
        channel.shutdown();
        try {
            if (!channel.awaitTermination(shutdownTimeoutMS, TimeUnit.SECONDS)) {
                channel.shutdownNow();
                LOGGER.warn("Channel failed to terminate, forcibly closing it.");
                if (!channel.awaitTermination(shutdownTimeoutMS, TimeUnit.SECONDS)) {
                    LOGGER.error("Channel failed to terminate.");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Unable to close the channel.", e);
        }
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

    public static final class Builder {
        private Format<com.expedia.open.tracing.Span> format;

        private StreamObserver<DispatchResult> observer;

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

        private Builder() {
            this.format = new ProtoBufFormat();
            this.observer = new GRPCAgentClientStreamObserver();
        }

        public Builder(ManagedChannel channel) {
            this();
            this.channel = channel;
        }

        public Builder(String host, int port) {
            this();
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

            return new GRPCAgentClient(format, managedChannel, stub, observer, shutdownTimeoutMS);
        }
    }
}
