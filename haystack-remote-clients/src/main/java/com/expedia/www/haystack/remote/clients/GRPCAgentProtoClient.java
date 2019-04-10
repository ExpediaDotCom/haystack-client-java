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


import com.expedia.open.tracing.Span;
import com.expedia.open.tracing.agent.api.DispatchResult;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc.SpanAgentStub;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Timer.Sample;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class GRPCAgentProtoClient extends BaseGrpcClient<Span> {
    public GRPCAgentProtoClient(Metrics metrics,
                                ManagedChannel channel,
                                SpanAgentStub stub,
                                StreamObserver<DispatchResult> observer,
                                long shutdownTimeoutMS) {
        super(metrics, channel, stub, observer, shutdownTimeoutMS);
    }

    @Override
    public boolean send(Span span) throws ClientException {
        try (Sample timer = sendTimer.start()) {
            stub.dispatch(span, observer);
        } catch (Exception e) {
            sendExceptionCounter.increment();
            throw new ClientException(e.getMessage(), e);
        }
        // always true
        return true;
    }

    public static final class Builder extends BaseGrpcClient.Builder {
        public Builder(MetricsRegistry metrics, ManagedChannel channel) {
            super(metrics, channel);
        }

        public Builder(Metrics metrics, String host, int port) {
            super(metrics, host, port);
        }

        public Builder(MetricsRegistry metrics, String host, int port) {
            super(metrics, host, port);
        }

        public GRPCAgentProtoClient build() {
            ManagedChannel managedChannel = this.channel;

            if (managedChannel == null) {
                managedChannel = buildManagedChannel();
            }

            SpanAgentStub stub = SpanAgentGrpc.newStub(managedChannel);
            return new GRPCAgentProtoClient(metrics, managedChannel, stub, observer, shutdownTimeoutMS);
        }
    }
}