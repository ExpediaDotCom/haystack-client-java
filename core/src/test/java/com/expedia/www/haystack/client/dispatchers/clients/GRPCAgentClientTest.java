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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import com.expedia.open.tracing.agent.api.DispatchResult;
import com.expedia.open.tracing.agent.api.SpanAgentGrpc;
import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;

import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcServerRule;

public class GRPCAgentClientTest {

    @Rule
    public final GrpcServerRule grpcServerRule = new GrpcServerRule().directExecutor();

    private final SpanAgentGrpc.SpanAgentImplBase serviceImpl = spy(new SpanAgentGrpc.SpanAgentImplBase() {});

    private GRPCAgentClient client;
    private Tracer tracer;

    @Before
    public void setup() throws Exception {
        NoopMetricsRegistry metrics = new NoopMetricsRegistry();
        // register the service
        grpcServerRule.getServiceRegistry().addService(serviceImpl);
        // build a client with the in-process channel
        client = new GRPCAgentClient.Builder(metrics, grpcServerRule.getChannel()).build();
        tracer = new Tracer.Builder(metrics, "grpc-agent-tests", new InMemoryDispatcher.Builder(metrics).build()).build();
    }

    @After
    public void teardown() throws Exception {
        client.close();
        tracer.close();
    }

    @Test
    public void testDispatch() throws Exception {
        final Span span = tracer.buildSpan("happy-path").start();
        span.finish();

        final ArgumentCaptor<com.expedia.open.tracing.Span> spanCapture = ArgumentCaptor.forClass(com.expedia.open.tracing.Span.class);

        client.send(span);

        verify(serviceImpl, times(1)).dispatch(spanCapture.capture(), Matchers.<StreamObserver<DispatchResult>>any());
    }

}
